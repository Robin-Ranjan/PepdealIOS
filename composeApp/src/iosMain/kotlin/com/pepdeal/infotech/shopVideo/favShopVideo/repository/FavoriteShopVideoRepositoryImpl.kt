package com.pepdeal.infotech.shopVideo.favShopVideo.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseRequest
import com.pepdeal.infotech.core.databaseUtils.DatabaseResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.EmptyResult
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsRepo
import com.pepdeal.infotech.shopVideo.ShopVideoWithShopDetail
import com.pepdeal.infotech.shopVideo.ShopVideosMaster
import com.pepdeal.infotech.shopVideo.favShopVideo.model.FavouriteShopVideo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FavoriteShopVideoRepositoryImpl(private val httpClient: HttpClient) :
    FavoriteShopVideoRepository {
    override suspend fun getFavoriteShopVideoForUserFlow(userId: String): Flow<AppResult<ShopVideoWithShopDetail?, DataError.Remote>> =
        flow {
            try {
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER,
                    filters = listOf(FirestoreFilter("userId", userId)),
                    limit = 50
                )

                val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> =
                    safeCall {
                        httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                            contentType(ContentType.Application.Json)
                            setBody(queryBody)
                        }
                    }

                if (response is AppResult.Error) {
                    emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER)))
                }

                val favouriteShopVideos =
                    (response as AppResult.Success).data.mapNotNull { result ->
                        result.document?.fields?.let { fields ->
                            FavouriteShopVideo(
                                shop_id = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                user_id = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                shop_video_id = (fields["shopVideoId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                favouriteShopVideoId = (fields["favouriteShopVideoId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                            )
                        }
                    }.sortedByDescending { it.createdAt.toLongOrNull() ?: 0L }

                if (favouriteShopVideos.isEmpty()) {
                    emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                    return@flow
                }

                for (favouriteShopVideo in favouriteShopVideos) {
                    val shopResult = ShopDetailsRepo().fetchShopDetails(favouriteShopVideo.shop_id)
                    val shopVideo = getShopVideo(favouriteShopVideo.shop_id)
                    if (shopResult != null && shopVideo != null) {
                        val shopVideoWithShopDetail = ShopVideoWithShopDetail(shopVideo, shopResult)
                        emit(AppResult.Success(shopVideoWithShopDetail))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println(e.message)
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            message = e.message
                        )
                    )
                )
            }
        }

    override suspend fun removeFavoriteShopVideo(
        userId: String,
        shopId: String
    ): Flow<EmptyResult<DataError.Remote>> = flow {
        try {
            val requestBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("shopId", shopId)
                ),
                limit = 1
            )

            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status != HttpStatusCode.OK) {
                println("Not Deleted")
                emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER)))
            }


            val databaseResponse: List<DatabaseQueryResponse> = try {
                AppJson.decodeFromString(response.bodyAsText())
            } catch (e: Exception) {
                listOf(AppJson.decodeFromString<DatabaseQueryResponse>(response.bodyAsText()))
            }

            if (databaseResponse.isEmpty()) {
                println("No matching favorite Shop Video found for deletion.")
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            "Something went wrong"
                        )
                    )
                )
            }
            // Extract document ID
            val documentPath = databaseResponse.firstOrNull()?.document?.name
            if (documentPath.isNullOrEmpty()) {
                println("No document name found in response")
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            "Something went wrong"
                        )
                    )
                )
            }

            val documentId = documentPath?.substringAfterLast("/")
            println("Extracted Document ID for deletion: $documentId")

            val deleteResponse =
                httpClient.delete("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("Deleted Shop Video Fav")
                emit(AppResult.Success(Unit))
            } else {
                println("Failed to delete favorite Shop Video. Status: ${deleteResponse.status}")
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            "Something went wrong"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            println("Exception occurred during delete: ${e.message}")
            emit(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        "Something went wrong"
                    )
                )
            )
        }
    }

    override suspend fun checkIfShopVideoIsFavorite(
        userId: String,
        shopId: String
    ): Result<Boolean> {
        return runCatching {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("shopId", shopId)
                )
            )

            val response = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }

            if (response.status != HttpStatusCode.OK) {
                throw Exception("Firestore query failed with status: ${response.status}")
            }

            val responseText = response.bodyAsText()
            if (responseText.isBlank()) return@runCatching false

            val queryResults: List<DatabaseQueryResponse> =
                AppJson.decodeFromString(responseText)

            // Return true if at least one matching document exists
            queryResults.any { it.document?.fields != null }
        }.onFailure { error ->
            println("❌ isFavorite failed: ${error.message}")
        }
    }

    override suspend fun addFavoriteShopVideo(shopVideo: FavouriteShopVideo): EmptyResult<DataError.Remote> {
        return try {
            val response =
                com.pepdeal.infotech.placeAPI.httpClient.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER}") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "userId" to DatabaseValue.StringValue(shopVideo.user_id),
                                "shopId" to DatabaseValue.StringValue(shopVideo.shop_id),
                                "shopVideoId" to DatabaseValue.StringValue(shopVideo.shop_video_id),
                                "favouriteShopVideoId" to DatabaseValue.StringValue(shopVideo.favouriteShopVideoId),
                                "createdAt" to DatabaseValue.StringValue(shopVideo.createdAt),
                            )
                        )
                    )
                }

            if (response.status != HttpStatusCode.OK) {
                println("Error: ${response.status} ${response.bodyAsText()}")
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }
            val databaseResponse: DatabaseResponse = response.body()
            val generatedId = databaseResponse.name.substringAfterLast("/")

            val patchResponse =
                com.pepdeal.infotech.placeAPI.httpClient.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER}/$generatedId?updateMask.fieldPaths=favouriteShopVideoId") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "favouriteShopVideoId" to DatabaseValue.StringValue(generatedId)
                            )
                        )
                    )
                }

            if (patchResponse.status == HttpStatusCode.OK) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.SERVER,
                    message = "Something went wrong"
                )
            )
        }
    }

    private suspend fun getShopVideo(shopId: String): ShopVideosMaster? {
        try {
            val query = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_VIDEOS_MASTER,
                filters = listOf(
                    FirestoreFilter("shopId", shopId),
                    FirestoreFilter("flag", "0"),
                    FirestoreFilter("isActive", "0")
                )
            )

            val videoQueryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> =
                safeCall {
                    httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(query)
                    }.body()
                }
            when (videoQueryResponse) {
                is AppResult.Success -> {
                    val validVideo =
                        videoQueryResponse.data.firstOrNull()?.document?.fields?.let { fields ->
                            ShopVideosMaster(
                                shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                videoUrl = (fields["videoUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                thumbNailUrl = (fields["thumbNailUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                shopVideoId = (fields["shopVideoId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                            )
                        }
                    return validVideo

                }

                is AppResult.Error -> {
                    println("Error: ${videoQueryResponse.error.message}")
                    return null
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: ${e.message}")
            return null
        }
    }
}
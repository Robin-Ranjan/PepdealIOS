package com.pepdeal.infotech.shopVideo

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
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.placeAPI.httpClient
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsRepo
import com.pepdeal.infotech.shopVideo.favShopVideo.model.FavouriteShopVideo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ShopVideosRepo {
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    fun fetchShopVideoWithShopDetailsFlow(): Flow<ShopVideoWithShopDetail> = flow {
        try {
            // Step 1: Query Firestore for videos with flag == "0" and isActive == "0"
            val query = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_VIDEOS_MASTER,
                filters = listOf(
                    FirestoreFilter("flag", "0"),
                    FirestoreFilter("isActive", "0")
                )
            )

            val videoQueryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> =
                safeCall {
                    client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(query)
                    }.body()
                }

            if (videoQueryResponse is AppResult.Error) {
                println("❌ Failed to fetch shop videos.")
                return@flow
            }

            if (videoQueryResponse is AppResult.Success) {
                println("✅ Shop videos fetched successfully.")
                val validVideos = videoQueryResponse.data.map { result ->
                    val fields = result.document?.fields ?: return@map null

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

                for (shopVideo in validVideos) {
                    shopVideo?.let {
                        val shopDetails = ShopDetailsRepo().fetchShopDetails(it.shopId)
                        if (shopDetails != null) {
                            println("❌ Failed to fetch shop details for shopId=${shopVideo.shopId}")
                            emit(ShopVideoWithShopDetail(it, shopDetails))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("🔥 Error: ${e.message}")
        }
    }


    suspend fun isSavedVideo(userId: String, shopId: String): Boolean {
        return try {
            // Query Firebase to get items matching the productId
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("shopId", shopId)
                )
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Success -> {
                    val found = response.data.any { it.document?.fields?.isNotEmpty() == true }
                    found
                }

                is AppResult.Error -> {
                    println("Firestore query error: ${response.error.message}")
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addSaveVideo(shopVideo: FavouriteShopVideo) {
        try {

            val response =
                httpClient.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER}") {
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
                httpClient.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER}/$generatedId?updateMask.fieldPaths=favouriteShopVideoId") {
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
        }
    }

    suspend fun removeFavoriteItem(userId: String, shopId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) { AppJson }
        }
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
            }


            val databaseResponse: List<DatabaseQueryResponse> = try {
                AppJson.decodeFromString(response.bodyAsText())
            } catch (e: Exception) {
                listOf(AppJson.decodeFromString<DatabaseQueryResponse>(response.bodyAsText()))
            }

            if (databaseResponse.isEmpty()) {
                println("No matching favorite doctor found for deletion.")
            }
            // Extract document ID
            val documentPath = databaseResponse.firstOrNull()?.document?.name
            if (documentPath.isNullOrEmpty()) {
                println("No document name found in response")
            }

            val documentId = documentPath?.substringAfterLast("/")
            println("Extracted Document ID for deletion: $documentId")

            val deleteResponse =
                httpClient.delete("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_SHOP_VIDEO_MASTER}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("Deleted Doctor Fav")
                onDelete()
            } else {
                println("Failed to delete favorite doctor. Status: ${deleteResponse.status}")
            }

        } catch (e: Exception) {
            println("❌ Error deleting favorite item: ${e.message}")
        } finally {
            client.close()
        }
    }
}
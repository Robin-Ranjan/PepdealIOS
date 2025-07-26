package com.pepdeal.infotech.favourite_product.repository

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
import com.pepdeal.infotech.favourite_product.modal.FavProductWithImages
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.product.repository.ProductRepository
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

class FavouriteProductRepositoryImpl(
    private val httpClient: HttpClient,
) :
    FavouriteProductRepository {
    override suspend fun fetchFavoriteProducts(userId: String): Flow<AppResult<FavProductWithImages, DataError.Remote>> =
        flow {
            try {
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.FAVOURITE_MASTER,
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
                    emit(AppResult.Error(response.error))
                    return@flow
                }

                val favorites =
                    (response as AppResult.Success).data.mapNotNull { result ->
                        result.document?.fields?.let { fields ->
                            FavoriteProductMaster(
                                favId = (fields["favId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                            )
                        }
                    }.sortedByDescending { it.updatedAt.toLongOrNull() ?: 0L }

                if (favorites.isEmpty()) {
                    emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                    return@flow
                }

                for (favorite in favorites) {
//                    val productResult = productRepo.value.fetchProductDetails(favorite.productId)
//                    if (productResult is AppResult.Error) continue
//                    val product = (productResult as AppResult.Success).data ?: continue
//
//                    val imageResult = productRepo.value.fetchProductImages(product.productId)
//                    val image = if (imageResult is AppResult.Success) imageResult.data else null
//                    emit(
//                        AppResult.Success(
//                            FavProductWithImages(
//                                product = product,
//                                images = image ?: emptyList()
//                            )
//                        )
//                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            message = e.message ?: "Unexpected error while fetching favorites"
                        )
                    )
                )
            }
        }

    override suspend fun removeFavoriteItem(
        userId: String,
        productId: String
    ): Flow<AppResult<Unit, DataError.Remote>> = flow {
        try {
            val requestBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("productId", productId)
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
                println("No matching favorite product found for deletion.")
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
                httpClient.delete("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_MASTER}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("Deleted product Fav")
                emit(AppResult.Success(Unit))
            } else {
                println("Failed to delete favorite product. Status: ${deleteResponse.status}")
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

    override suspend fun isFavorite(userId: String, productId: String): Result<Boolean> {
        return runCatching {
            val queryBody = buildFirestoreQuery(
                collection = "favourite_master",
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("productId", productId)
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

    override suspend fun addFavorite(product: FavoriteProductMaster): EmptyResult<DataError.Remote> {
        return try {
            val response =
                httpClient.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_MASTER}") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "userId" to DatabaseValue.StringValue(product.userId),
                                "productId" to DatabaseValue.StringValue(product.productId),
                                "createdAt" to DatabaseValue.StringValue(product.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(product.updatedAt)
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
                httpClient.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_MASTER}/$generatedId?updateMask.fieldPaths=favId") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "id" to DatabaseValue.StringValue(generatedId)
                            )
                        )
                    )
                }

            if (patchResponse.status == HttpStatusCode.OK) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        message = "Something went wrong"
                    )
                )
            }
        } catch (e: Exception) {
            println(e.message)
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.SERVER,
                    message = "Something went wrong"
                )
            )
        }
    }

}
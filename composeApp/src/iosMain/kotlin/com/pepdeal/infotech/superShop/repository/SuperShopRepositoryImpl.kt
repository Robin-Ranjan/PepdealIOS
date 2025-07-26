package com.pepdeal.infotech.superShop.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.EmptyResult
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.shop.repository.ShopRepository
import com.pepdeal.infotech.superShop.model.SuperShopMaster
import com.pepdeal.infotech.superShop.model.SuperShopsWithProduct
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SuperShopRepositoryImpl(
    private val httpClient: HttpClient,
    private val shopRepo: ShopRepository,
    private val productRepo: ProductRepository
) : SuperShopRepository {

    override suspend fun getSuperShopWithProduct(userId: String): Flow<AppResult<SuperShopsWithProduct, DataError.Remote>> =
        flow {
            try {
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.SUPER_SHOP_MASTER,
                    filters = listOf(FirestoreFilter("userId", userId)),
                    limit = 50
                )

                val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                    httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(queryBody)
                    }
                }

                if (response is AppResult.Error) {
                    emit(AppResult.Error(response.error))
                    return@flow
                }

                val superShops =
                    (response as AppResult.Success).data.mapNotNull { result ->
                        result.document?.fields?.let { fields ->
                            SuperShopMaster(
                                superId = (fields["superId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            )
                        }
                    }.sortedByDescending { it.updatedAt.toLongOrNull() ?: 0L }

                if (superShops.isEmpty()) {
                    emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                    return@flow
                }

                for ((index, superShop) in superShops.withIndex()) {
                    println("🔄 [$index] Processing SuperShop: ${superShop.shopId}")

                    val shopDetailsResult = shopRepo.fetchShopDetails(superShop.shopId)
                    if (shopDetailsResult is AppResult.Error) {
                        println("❌ [$index] Failed to fetch shop details for shopId = ${superShop.shopId}")
                        continue
                    }

                    val shopDetails = (shopDetailsResult as AppResult.Success).data
                    if (shopDetails == null) {
                        println("⚠️ [$index] Shop details are null for shopId = ${superShop.shopId}")
                        continue
                    }
                    println("✅ [$index] Shop details fetched: ${shopDetails.shopName}")

                    val productsWithImagesResult =
                        productRepo.getActiveProductsWithImages(superShop.shopId)
                    if (productsWithImagesResult is AppResult.Error) {
                        println("❌ [$index] Failed to fetch products for shopId = ${superShop.shopId}")
                        continue
                    }

                    val productsWithImages = (productsWithImagesResult as AppResult.Success).data
                    println("🛒 [$index] Products fetched for shopId = ${superShop.shopId}: ${productsWithImages.size}")

                    emit(
                        AppResult.Success(
                            SuperShopsWithProduct(
                                shop = shopDetails,
                                products = productsWithImages,
                                createdAt = superShop.createdAt
                            )
                        )
                    )
                    println("🎉 [$index] Emitted SuperShopsWithProduct for shopId = ${superShop.shopId}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.NOT_FOUND,
                            message = e.message
                        )
                    )
                )
            }
        }

    override suspend fun removeSuperShop(
        userId: String,
        shopId: String
    ): EmptyResult<DataError.Remote> {
        return try {
            val requestBody = buildFirestoreQuery(
                collection = DatabaseCollection.SUPER_SHOP_MASTER,
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
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }


            val databaseResponse: List<DatabaseQueryResponse> = try {
                AppJson.decodeFromString(response.bodyAsText())
            } catch (e: Exception) {
                listOf(AppJson.decodeFromString<DatabaseQueryResponse>(response.bodyAsText()))
            }

            if (databaseResponse.isEmpty()) {
                println("No matching favorite doctor found for deletion.")
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }
            // Extract document ID
            val documentPath = databaseResponse.firstOrNull()?.document?.name
            if (documentPath.isNullOrEmpty()) {
                println("No document name found in response")
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }

            val documentId = documentPath?.substringAfterLast("/")
            println("Extracted Document ID for deletion: $documentId")

            val deleteResponse =
                httpClient.delete("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SUPER_SHOP_MASTER}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("Deleted Doctor Fav")
                AppResult.Success(Unit)
            } else {
                println("Failed to delete favorite doctor. Status: ${deleteResponse.status}")
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }
        } catch (e: Exception) {
            println("Exception occurred during delete: ${e.message}")
            AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
        }
    }
}
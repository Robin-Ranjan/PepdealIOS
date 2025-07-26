package com.pepdeal.infotech.product.listProduct.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseRequest
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestorePatchUrl
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.product.ListAllProductRepository
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ListAllProductRepositoryImpl(private val httpClient: HttpClient) : ListAllProductRepository {
    override suspend fun fetchAllProductOfShop(
        shopId: String
    ): Flow<AppResult<List<ProductWithImages>, DataError.Remote>> = flow {
        try {
            val filters = listOf(
                FirestoreFilter("shopId", shopId),
                FirestoreFilter("productActive", "0"),
                FirestoreFilter("flag", "0")
            )

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.PRODUCT_MASTER,
                filters = filters
            )

            println("📤 Sending query to Firestore for products...")

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }
            }

            if (response is AppResult.Error) {
                println("❌ Error fetching products: ${response.error.message}")
                emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER)))
                return@flow
            }

            val queryResults = (response as AppResult.Success).data
            println("✅ Products fetched: ${queryResults.size}")

            val productList = mutableListOf<ProductWithImages>()

            coroutineScope {
                val jobs = queryResults.mapNotNull { result ->
                    val fields = result.document?.fields ?: return@mapNotNull null

                    val product = ProductMaster(
                        productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        productName = (fields["productName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        brandId = (fields["brandId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        brandName = (fields["brandName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        categoryId = (fields["categoryId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        subCategoryId = (fields["subCategoryId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        description2 = (fields["description2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        specification = (fields["specification"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        warranty = (fields["warranty"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        sizeId = (fields["sizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        sizeName = (fields["sizeName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        color = (fields["color"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values
                            ?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                            .orEmpty(),
                        onCall = (fields["onCall"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        mrp = (fields["mrp"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        discountMrp = (fields["discountMrp"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        sellingPrice = (fields["sellingPrice"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        productActive = (fields["productActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopBlock = (fields["shopBlock"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopLongitude = (fields["shopLongitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopLatitude = (fields["shopLatitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                    )

                    async {
                        val images = getProductImages(product.productId)
                        productList.add(ProductWithImages(product, images))
                        println("🖼️ Product ${product.productId} with ${images.size} images added.")
                    }
                }

                jobs.awaitAll()
            }

            println("📦 Emitting final product list of size: ${productList.size}")
            emit(AppResult.Success(productList))

        } catch (e: Exception) {
            println("❌ Exception in getActiveProductsWithImages: ${e.message}")
            e.printStackTrace()
            emit(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        message = e.message ?: "Unknown error"
                    )
                )
            )
        }
    }

    override suspend fun updateProductStatusByShopOwner(
        productId: String,
        status: String
    ): Flow<AppResult<Unit, DataError.Remote>> = flow {
        try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.PRODUCT_MASTER,
                filters = listOf(
                    FirestoreFilter("productId", productId),
                ),
                limit = 1
            )

            // Step 2: Execute query
            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (response is AppResult.Error) {
                emit(response)
                return@flow
            }

            val documents = (response as AppResult.Success).data
            if (documents.isEmpty() || documents.first().document?.name.isNullOrEmpty()) {
                emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                return@flow
            }

            val documentName = documents.first().document!!.name
            val documentId = documentName.substringAfterLast("/")

            val updateFields = mapOf(
                "isActive" to DatabaseValue.StringValue(status),
                "updatedAt" to DatabaseValue.StringValue(Util.getCurrentTimeStamp())
            )

            val patchUrl = buildFirestorePatchUrl(
                collection = DatabaseCollection.PRODUCT_MASTER,
                documentId = documentId,
                fields = updateFields.keys.toList()
            )

            val patchResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.patch(patchUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(DatabaseRequest(fields = updateFields))
                }
            }

            when (patchResponse) {
                is AppResult.Success -> {
                    emit(AppResult.Success(Unit))
                }

                is AppResult.Error -> {
                    emit(
                        AppResult.Error(
                            DataError.Remote(
                                type = DataError.RemoteType.SERVER,
                                message = patchResponse.error.message
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    private suspend fun getProductImages(productId: String): List<ProductImageMaster> =
        coroutineScope {
            println("🔍 Fetching images for productId = $productId")

            val client = HttpClient(Darwin) {
                install(ContentNegotiation) { AppJson }
            }

            val imageList = mutableListOf<ProductImageMaster>()

            try {
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.PRODUCT_IMAGES_MASTER,
                    filters = listOf(FirestoreFilter("productId", productId))
                )

                println("📤 Sending image query to Firestore...")

                val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                    client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(queryBody)
                    }.body()
                }

                when (response) {
                    is AppResult.Error -> {
                        println("❌ Image fetch error: ${response.error.message}")
                    }

                    is AppResult.Success -> {
                        val images: List<ProductImageMaster> = response.data.mapNotNull { result ->
                            val fields = result.document?.fields ?: return@mapNotNull null
                            ProductImageMaster(
                                productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                productImages = (fields["productImages"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                            )
                        }

                        println("🖼️ Images found: ${images.size} for productId = $productId")
                        return@coroutineScope images
                    }
                }
            } catch (e: Exception) {
                println("❌ Exception in getProductImages: ${e.message}")
            } finally {
                println("📴 Closing client after fetching images for $productId")
                client.close()
            }

            return@coroutineScope imageList
        }
}
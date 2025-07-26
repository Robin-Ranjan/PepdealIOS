package com.pepdeal.infotech.shop.shopDetails

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
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.superShop.model.SuperShopMaster
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ShopDetailsRepo {
    private val httpClient = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }

    suspend fun fetchShopDetails(shopId: String): ShopMaster? {
        return try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                limit = 1,
                filters = listOf(
                    FirestoreFilter("shopId", shopId),
                    FirestoreFilter("shopActive", "0"),
                    FirestoreFilter("flag", "0")
                )
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }
            when (response) {
                is AppResult.Error -> null

                is AppResult.Success -> {
                    val shop = response.data.firstOrNull()?.document?.fields?.let { fields ->
                        ShopMaster(
                            shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopName = (fields["shopName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopMobileNo = (fields["shopMobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopAddress = (fields["shopAddress"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopAddress2 = (fields["shopAddress2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopArea = (fields["shopArea"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            city = (fields["city"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            state = (fields["state"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            pinCode = (fields["pinCode"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopDescription = (fields["shopDescription"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            bgColourId = (fields["bgColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            fontSizeId = (fields["fontSizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            fontStyleId = (fields["fontStyleId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            fontColourId = (fields["fontColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            latitude = (fields["latitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            longitude = (fields["longitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopStatus = (fields["shopStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            isVerified = (fields["isVerified"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            showNumber = (fields["showNumber"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            geoHash = (fields["geoHash"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()

                        )
                    }
                    return shop
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getActiveProductsWithImages(shopId: String): Flow<List<ProductWithImages>> = channelFlow {
        println("🌐 getActiveProductsWithImages() called for shopId = $shopId")

        val client = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

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
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }
            }

            val queryResults = when (response) {
                is AppResult.Error -> {
                    println("❌ Error fetching products: ${response.error.message}")
                    return@channelFlow
                }

                is AppResult.Success -> response.data
            }

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
                        searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
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
            send(productList)

        } catch (e: Exception) {
            println("❌ Exception in getActiveProductsWithImages: ${e.message}")
            e.printStackTrace()
        } finally {
            println("📴 Closing HTTP client for getActiveProductsWithImages")
            client.close()
        }

        awaitClose { println("🔚 Channel closed in getActiveProductsWithImages") }
    }

    // Fetch images for a specific product
    suspend fun getProductImages(productId: String): List<ProductImageMaster> = coroutineScope {
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
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
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

    // to get the shop Id by using shoId to show in shop Details page
    suspend fun fetchShopServices(shopId: String): ShopStatusMaster? = withContext(Dispatchers.IO) {
        try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_STATUS_MASTER,
                limit = 1,
                filters = listOf(
                    FirestoreFilter("shopId", shopId),
                )
            )
            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Error -> return@withContext null

                is AppResult.Success -> {
                    val shopStatusMaster =
                        response.data.firstOrNull()?.document?.fields?.let { fields ->
                            ShopStatusMaster(
                                shopStatusId = (fields["shopStatusId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                cashOnDelivery = (fields["cashOnDelivery"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                doorStep = (fields["doorStep"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                homeDelivery = (fields["homeDelivery"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                liveDemo = (fields["liveDemo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                offers = (fields["offers"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                bargain = (fields["bargain"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            )
                        }
                    return@withContext shopStatusMaster
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun addSuperShop(superShop: SuperShopMaster) {
        try {
            val client = HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
            }

            val response =
                client.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SUPER_SHOP_MASTER}") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "superId" to DatabaseValue.StringValue(superShop.superId),
                                "userId" to DatabaseValue.StringValue(superShop.userId),
                                "shopId" to DatabaseValue.StringValue(superShop.shopId),
                                "createdAt" to DatabaseValue.StringValue(superShop.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(superShop.updatedAt)
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
                client.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SUPER_SHOP_MASTER}/$generatedId?updateMask.fieldPaths=superId") {
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
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun checkSuperShopExists(
        shopId: String,
        userId: String,
        callback: (exists: Boolean) -> Unit
    ) {
        try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SUPER_SHOP_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("shopId", shopId)
                )
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Success -> {
                    val found = response.data.any { it.document?.fields?.isNotEmpty() == true }
                    callback(found)
                }

                is AppResult.Error -> {
                    println("Firestore query error: ${response.error.message}")
                    callback(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            callback(false)
        }
    }

    suspend fun removeSuperShop(userId: String, shopId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
        try {
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
                client.delete("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SUPER_SHOP_MASTER}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("Deleted Doctor Fav")
                onDelete()
            } else {
                println("Failed to delete favorite doctor. Status: ${deleteResponse.status}")
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }
        } catch (e: Exception) {
            println("❌ Error deleting favorite item: ${e.message}")
        } finally {
            client.close()
        }
    }

}
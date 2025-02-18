package com.pepdeal.infotech.shop

import com.pepdeal.infotech.shop.modal.ProductImageMaster
import com.pepdeal.infotech.shop.modal.ProductMaster
import com.pepdeal.infotech.shop.modal.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.experimental.ExperimentalNativeApi


class ShopRepo {

    val json = Json{ignoreUnknownKeys =true}
    fun getActiveShopsFlow(): Flow<ShopWithProducts> = callbackFlow {
        val client = HttpClient(Darwin)

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                // Deserialize JSON into Map
                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)
                println("shop ${shopsMap.values}")
                coroutineScope {
                    val deferredShops = shopsMap.map { (_, shop) ->
                        async {
                            println("calling product function 1")
                            if (shop.isActive == "0" && shop.flag == "0") {
                                println("shopId:- ${shop.shopName}")
                                println("calling product function")
                                val products = getActiveProductsWithImages(shop.shopId ?: "-1")
                                if (products.isNotEmpty()) {
                                    ShopWithProducts(shop, products)
                                } else {
                                    null
                                }
                            } else null
                        }
                    }

                    deferredShops.awaitAll().filterNotNull().forEach { shopWithProducts ->
                        trySend(shopWithProducts) // Emit shop with products
                    }
                }
            } else {
                close(Exception("Error fetching shop data: ${response.status}"))
            }
        } catch (e: Exception) {
            close(e) // Close the flow on error
        } finally {
            client.close()
        }

        awaitClose { }
    }

    private suspend fun getActiveProductsWithImages(shopId: String): List<ProductWithImages> {
        val client = HttpClient(Darwin)
        val productList = mutableListOf<ProductWithImages>()
        println("product function")
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\"") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
                println("product ${responseBody.length}")
                println("product response ${responseBody.toString()}")
                // Use coroutineScope to launch the product image fetching concurrently
                coroutineScope {
                    productsMap.values.forEach { product ->
                        if (product.isActive == "0" && product.flag == "0") {
                            async {
                                println("product ${product.productName}")
                                val images = getProductImages(product.productId)
                                val productWithImages = ProductWithImages(product, images)
                                productList.add(productWithImages)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error fetching product data: ${e.message}")
        } finally {
            client.close()
        }

        return productList
    }


    // Fetch images for a specific product
    private suspend fun getProductImages(productId: String): List<ProductImageMaster> = coroutineScope {
        val client = HttpClient(Darwin)
        val imageList = mutableListOf<ProductImageMaster>()

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(responseBody)

                imageList.addAll(imagesMap.values)
            }
        } catch (e: Exception) {
            println("Error fetching product images: ${e.message}")
        } finally {
            client.close()
        }
        return@coroutineScope imageList
    }




    fun getShopsHttp(): Flow<ProductMaster> = callbackFlow {
        val client = HttpClient(Darwin)  // Specify the iOS engine
        var count = 0
        try {
            // Making the HTTP GET request to Firebase Realtime Database
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&equalTo=\"43f034a4-42e2-4c79-85d8-3eb88a9b3658\""){
                contentType(ContentType.Application.Json)
            }


            // Get the response body as a String
            val responseBody: String = response.bodyAsText()
            println("${responseBody.length} ${response.status}")

            // Deserialize the response into a Map (String -> ProductMaster)
            val json = Json { ignoreUnknownKeys = true }  // Add this configuration to ignore unknown keys
            val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)


            // Emit each ProductMaster from the map
            for ((_, product) in productsMap) {
                count++
                println("${product.productName} $count")
                trySend(product)  // Emit each product one by one
            }
        } catch (e: Exception) {
            close(e)  // Close the flow with an exception if something goes wrong
        } finally {
            client.close()  // Close the client after the operation
        }

        awaitClose { }
    }

    fun getActiveShopsFlowPagination(lastShopId: String? = null, pageSize: Int = 10): Flow<List<ShopWithProducts>> = flow {
        val client = HttpClient(Darwin)
        try {
            val url = if (lastShopId != null) {
                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&startAt=\"$lastShopId\"&limitToFirst=$pageSize"
            } else {
                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&limitToFirst=$pageSize"
            }
            println("Fetching shops from: $url")

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)
                val activeShops = shopsMap.values.filter { it.isActive == "0" && it.flag == "0" }

                coroutineScope {
                    val deferredShops = activeShops.map { shop ->
                        async {
                            val products = getActiveProductsWithImages(shop.shopId ?: "-1")
                            if (products.isNotEmpty()) ShopWithProducts(shop, products) else null
                        }
                    }

                    val shopsWithProducts = deferredShops.awaitAll().filterNotNull()

                    if (shopsWithProducts.isNotEmpty()) {
                        emit(shopsWithProducts) // Emit paginated shops
                    }
                }
            } else {
                throw Exception("Error fetching shop data: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }


    fun getActiveShopsFlowPaginationEmit(
        lastShopId: String? = null,
        pageSize: Int = 10
    ): Flow<ShopWithProducts> = flow {
        val client = HttpClient(Darwin)

        try {
            val url = if (lastShopId != null) {
                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&startAt=\"$lastShopId\"&limitToFirst=$pageSize"
            } else {
                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&limitToFirst=$pageSize"
            }
            println("Fetching shops from: $url")

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)
                val activeShops = shopsMap.values.filter { it.isActive == "0" && it.flag == "0" }

                for (shop in activeShops) {
                    val products = getActiveProductsWithImages(shop.shopId ?: "-1")
                    if (products.isNotEmpty()) {
                        emit(ShopWithProducts(shop, products)) // Emit each shop immediately
                    }
                }
            } else {
                throw Exception("Error fetching shop data: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    fun getActiveShopsFlowPaginationEmitWithFilter(
        lastShopId: String? = null,
        pageSize: Int = 10
    ): Flow<ShopWithProducts> = flow {
        val client = HttpClient(Darwin)

        try {
            // Construct the Firebase query URL, adding flag filter directly to the URL
            val url = if (lastShopId != null) {
                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&startAt=\"$lastShopId\"&limitToFirst=$pageSize"
            } else {
                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&limitToFirst=$pageSize"
            }

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                // Parsing the response JSON into a Map of shopId -> ShopMaster
                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)

                val activeShops = shopsMap.values.filter { it.flag == "0" }
                println("shops:- ${shopsMap.values}")
                // Iterate over the filtered shops and emit them
                for (shop in activeShops) {
                    // Filter products with active flag
                    val products = getActiveProductsWithImages(shop.shopId ?: "-1")
                    if (products.isNotEmpty()) {
                        emit(ShopWithProducts(shop, products)) // Emit each shop with its products
                    }
                }
            } else {
                throw Exception("Error fetching shop data: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
    }


}
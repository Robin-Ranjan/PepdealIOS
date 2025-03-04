package com.pepdeal.infotech.yourShop

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class YourShopRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin)

    suspend fun fetchShopDetails(shopId: String): ShopMaster? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master/$shopId.json") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                json.decodeFromString(response.bodyAsText())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getActiveProductsWithImages(shopId: String) = channelFlow {
        val client = HttpClient(Darwin)
        println("product function")
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\"") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val productsMap: Map<String, ProductMaster> = Json.decodeFromString(responseBody)
                println("product ${responseBody.length}")
                println("product response $responseBody")

                // Fetch images for each product concurrently
                coroutineScope {
                    productsMap.values.forEach { product ->
//                        if (product.isActive == "0" && product.flag == "0") {
                            launch {
                                try {
                                    println("Fetching images for ${product.productName}")
                                    val images = getProductImages(product.productId)
                                    val productWithImages = ProductWithImages(product, images)
                                    send(productWithImages)  // ✅ Use `send()` instead of `emit()`
                                } catch (e: Exception) {
                                    println("Error fetching product images: ${e.message}")
                                }
//                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error fetching product data: ${e.message}")
        } finally {
            client.close()
        }

        awaitClose { println("Channel closed") }  // Ensure proper cleanup when the flow collection stops
    }

    // Fetch images for a specific product
    private suspend fun getProductImages(productId: String): List<ProductImageMaster> = coroutineScope {
        val client = HttpClient(Darwin)
        val imageList = mutableListOf<ProductImageMaster>()

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
//                parameter("orderBy", "\"productId\"")
//                parameter("equalTo", "\"$productId\"")
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

    // to get the shop Id by using shoId to show in shop Details page
    suspend fun fetchShopServices(shopId: String): ShopStatusMaster? = withContext(Dispatchers.IO) {
        return@withContext try {
            val statusResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_status_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\"") {
                contentType(ContentType.Application.Json)
            }

            if (statusResponse.status == HttpStatusCode.OK) {
                val statusBody: String = statusResponse.bodyAsText()
                val statusMap: Map<String, ShopStatusMaster> = json.decodeFromString(statusBody)
                statusMap.values.firstOrNull() // Returns null if no entry is found
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
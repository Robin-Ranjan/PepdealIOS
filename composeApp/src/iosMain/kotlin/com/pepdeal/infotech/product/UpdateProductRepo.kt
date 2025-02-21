package com.pepdeal.infotech.product

import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class UpdateProductRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetchProductDetails(productId: String): ProductMaster? {
        val productDetails: ProductMaster?
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
//                parameter("orderBy", "productId")
//                parameter("equalTo", productId)
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val productMap: Map<String, ProductMaster> =
                    json.decodeFromString(response.bodyAsText())
                productMap.values.firstOrNull() ?: ProductMaster()
                productDetails = productMap.values.firstOrNull()
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            return null
        }
        return productDetails
    }

    // Fetch images for a specific product
    suspend fun getProductImages(productId: String): List<ProductImageMaster> = coroutineScope {
        val client = HttpClient(Darwin)
        val imageList = mutableListOf<ProductImageMaster>()

        try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
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
}
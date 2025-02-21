package com.pepdeal.infotech.product

import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ListAllProductRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin)

    fun fetchAllProductOfShop(shopId: String): Flow<ProductWithImages> = flow {
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                parameter("orderBy", "\"shopId\"")
                parameter("equalTo", "\"$shopId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val productMap: Map<String, ProductMaster> =
                    json.decodeFromString(response.bodyAsText())
                 val productList = productMap.values.toList().sortedByDescending {
                    it.updatedAt.toLongOrNull() ?: 0L
                }
                for (product in productList) {
                    val imageList = mutableListOf<ProductImageMaster>()
                    val image = fetchProductImage(product.productId)
                    if (image != null) {
                        imageList.add(image)
                        emit(ProductWithImages(product, imageList.toList()))
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

    private suspend fun fetchProductImage(productId: String): ProductImageMaster? {
        return try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_images_master.json") {
                    parameter("orderBy", "\"productId\"")
                    parameter("equalTo", "\"$productId\"")
                    contentType(ContentType.Application.Json)
                }

            if (response.status == HttpStatusCode.OK) {
                val imageMap: Map<String, ProductImageMaster> =
                    json.decodeFromString(response.bodyAsText())
                imageMap.values.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
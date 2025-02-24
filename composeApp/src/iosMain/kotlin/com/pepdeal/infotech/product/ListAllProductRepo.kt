package com.pepdeal.infotech.product

import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class ListAllProductRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

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


    suspend fun updateProductStatusByShopOwner(
        productId: String,
        status: String,
        onSuccess: (Boolean) -> Unit
    ) {
        try {
            val updateMap = mapOf(
                "isActive" to status,
                "updatedAt" to Util.getCurrentTimeStamp()
            )
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
                    contentType(ContentType.Application.Json)
                }

            val responseBody = response.bodyAsText()
            println("response Body :_ $responseBody")
            if (responseBody.isEmpty() || responseBody == "null") {
                onSuccess(false)
            }

            // Parse the response to extract the node key.
            val nodeMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
            val nodeKey = nodeMap.keys.firstOrNull()
            if (nodeKey == null) {
                onSuccess(false)
            }

            // Step 2: Update the product using the node key.
            val updateUrl = "${FirebaseUtil.BASE_URL}product_master/$nodeKey.json"
            val jsonBody = json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                updateMap
            )
            val patchResponse: HttpResponse = client.patch(updateUrl) {
                contentType(ContentType.Application.Json)
                setBody(TextContent(jsonBody, ContentType.Application.Json))
            }

            onSuccess(patchResponse.status.isSuccess())
        } catch (e:Exception){
            e.printStackTrace()
            println(e.message)
        }
    }

}
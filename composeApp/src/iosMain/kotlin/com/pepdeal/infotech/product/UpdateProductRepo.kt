package com.pepdeal.infotech.product

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
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
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
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
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
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

    // Main function to update product details and replace images
    suspend fun updateProductWithImages(
        productId: String,
        updatedProductMaster: ProductMaster,
        isImageEdited: Boolean,
        newUriList: MutableList<ImageBitmap>,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            val updateMap: Map<String, String> = mapOf(
                "productName" to updatedProductMaster.productName,
                "brandName" to updatedProductMaster.brandName,
                "categoryId" to updatedProductMaster.categoryId,
                "subCategoryId" to updatedProductMaster.subCategoryId,
                "description" to updatedProductMaster.description,
                "description2" to updatedProductMaster.description2,
                "specification" to updatedProductMaster.specification,
                "warranty" to updatedProductMaster.warranty,
                "color" to updatedProductMaster.color,
                "mrp" to updatedProductMaster.mrp,
                "searchTag" to updatedProductMaster.searchTag,
                "onCall" to updatedProductMaster.onCall,
                "discountMrp" to updatedProductMaster.discountMrp,
                "sellingPrice" to updatedProductMaster.sellingPrice,
                "sizeName" to updatedProductMaster.sizeName,
                "updatedAt" to updatedProductMaster.updatedAt,
                "isActive" to updatedProductMaster.isActive,
                "flag" to updatedProductMaster.flag
            )

            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
                    contentType(ContentType.Application.Json)
                }

            val responseBody = response.bodyAsText()
            println("response Body :_ $responseBody")
            if (responseBody.isEmpty() || responseBody == "null") {
                onComplete(false, "Product not found")
            }

            // Parse the response to extract the node key.
            val nodeMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
            val nodeKey = nodeMap.keys.firstOrNull()
            if (nodeKey == null) {
                onComplete(false, "Product not found")
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

            if (!patchResponse.status.isSuccess()) {
                onComplete(false, "Failed to update product details: ${patchResponse.status}")
            } else {
                onComplete(true, "Product Details Updated")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            onComplete(false, e.message.toString())
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
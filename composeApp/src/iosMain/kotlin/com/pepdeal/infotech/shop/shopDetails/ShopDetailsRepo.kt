package com.pepdeal.infotech.shop.shopDetails

import com.pepdeal.infotech.shop.modal.ProductImageMaster
import com.pepdeal.infotech.shop.modal.ProductMaster
import com.pepdeal.infotech.shop.modal.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.superShop.SuperShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ShopDetailsRepo {
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
                        if (product.isActive == "0" && product.flag == "0") {
                            launch {
                                try {
                                    println("Fetching images for ${product.productName}")
                                    val images = getProductImages(product.productId)
                                    val productWithImages = ProductWithImages(product, images)
                                    send(productWithImages)  // ✅ Use `send()` instead of `emit()`
                                } catch (e: Exception) {
                                    println("Error fetching product images: ${e.message}")
                                }
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

    suspend fun checkSuperShopExists(shopId:String, userId:String,callback: (exists: Boolean) -> Unit) {

        try{
            // Query Firebase to get items matching the productId
            val queryUrl = "${FirebaseUtil.BASE_URL}super_shop_master.json?orderBy=\"userId\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body() // Fetch response as String

            // Parse JSON manually
            val responseJson = Json.parseToJsonElement(responseString).jsonObject

            // Check if any entry has the matching userId
            val exists = responseJson.values.any { jsonElement ->
                val superShopItem = jsonElement.jsonObject
                superShopItem["shopId"]?.jsonPrimitive?.content == shopId
            }
            callback(exists)
        }catch (e:Exception){
            e.printStackTrace()
            println(e.message)
            callback(false)
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

            val response: HttpResponse = client.post("${FirebaseUtil.BASE_URL}super_shop_master.json") {
                contentType(ContentType.Application.Json)
                setBody(superShop)
            }

            // Extract the unique key (favId) from Firebase response
            val responseBody = response.body<Map<String, String>>()
            val superShopId = responseBody["name"] ?: return // "name" contains the generated key

            // Update the favorite entry with the generated favId
            client.patch("${FirebaseUtil.BASE_URL}super_shop_master/$superShopId.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("superId" to superShopId))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeSuperShop(userId: String, shopId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin)
        try {
            // 1️⃣ Query Firebase to get all items for the user
            val queryUrl = "${FirebaseUtil.BASE_URL}super_shop_master.json?orderBy=\"userId\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body()  // Fetch as String

            // 2️⃣ Parse JSON manually
            val responseJson = Json.parseToJsonElement(responseString).jsonObject

            // 3️⃣ Find the correct favId by filtering on productId
            val superShopId = responseJson.entries.firstOrNull { (_, jsonElement) ->
                jsonElement.jsonObject["shopId"]?.jsonPrimitive?.content == shopId
            }?.key

            if (superShopId != null) {
                // 4️⃣ Delete the specific item using favId
                val deleteUrl = "${FirebaseUtil.BASE_URL}super_shop_master/$superShopId.json"
                val deleteResponse: HttpResponse = client.delete(deleteUrl)

                if (deleteResponse.status == HttpStatusCode.OK) {
                    println("✅ Favorite item deleted successfully.")
                    onDelete()
                } else {
                    println("❌ Failed to delete favorite item: ${deleteResponse.status}")
                }
            } else {
                println("⚠️ No matching favorite item found for productId: $shopId")
            }
        } catch (e: Exception) {
            println("❌ Error deleting favorite item: ${e.message}")
        } finally {
            client.close()
        }
    }

}
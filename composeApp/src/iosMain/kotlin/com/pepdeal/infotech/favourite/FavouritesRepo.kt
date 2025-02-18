package com.pepdeal.infotech.favourite

import com.pepdeal.infotech.shop.modal.FavProductWithImages
import com.pepdeal.infotech.shop.modal.ProductImageMaster
import com.pepdeal.infotech.shop.modal.ProductMaster
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FavouritesRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin)

    private suspend fun fetchFavoriteProducts(userId: String): List<FavoriteProductMaster> {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}favourite_master.json") {
                parameter("orderBy", "\"userId\"")
                parameter("equalTo", "\"$userId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val favMap: Map<String, FavoriteProductMaster> = json.decodeFromString(response.bodyAsText())
                favMap.values.toList()
            } else {
                emptyList()
            }


        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchProductDetails(productId: String): ProductMaster? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                parameter("orderBy", "\"productId\"")
                parameter("equalTo", "\"$productId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val productMap: Map<String, ProductMaster> = json.decodeFromString(response.bodyAsText())
                productMap.values.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun fetchShopDetails(shopId: String): ShopMaster? {
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

    private suspend fun fetchProductImage(productId: String): ProductImageMaster? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json") {
                parameter("orderBy", "\"productId\"")
                parameter("equalTo", "\"$productId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val imageMap: Map<String, ProductImageMaster> = json.decodeFromString(response.bodyAsText())
                imageMap.values.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFavoriteProductsForUserFlow(userId: String): Flow<FavProductWithImages> = flow {
        val favoriteProducts = fetchFavoriteProducts(userId)
            .sortedByDescending { it.createdAt.toLongOrNull() ?: 0L }

        for (favorite in favoriteProducts) {
            val product = fetchProductDetails(favorite.productId) ?: continue

            if (product.isActive == "0" && product.flag == "0") {
                val shop = fetchShopDetails(product.shopId)

                if (shop?.flag == "0" && shop.isActive == "0") {
                    val image = fetchProductImage(product.productId)
                    if (image != null) {
                        emit(FavProductWithImages(product, listOf(image),favorite.createdAt))
                    }
                }
            }
        }
    }

    suspend fun removeFavoriteItem(userId: String, productId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin)
        try {
            // 1️⃣ Query Firebase to get all items for the user
            val queryUrl = "${FirebaseUtil.BASE_URL}favourite_master.json?orderBy=\"userId\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body()  // Fetch as String

            // 2️⃣ Parse JSON manually
            val responseJson = Json.parseToJsonElement(responseString).jsonObject

            // 3️⃣ Find the correct favId by filtering on productId
            val favId = responseJson.entries.firstOrNull { (_, jsonElement) ->
                jsonElement.jsonObject["productId"]?.jsonPrimitive?.content == productId
            }?.key

            if (favId != null) {
                // 4️⃣ Delete the specific item using favId
                val deleteUrl = "${FirebaseUtil.BASE_URL}favourite_master/$favId.json"
                val deleteResponse: HttpResponse = client.delete(deleteUrl)

                if (deleteResponse.status == HttpStatusCode.OK) {
                    println("✅ Favorite item deleted successfully.")
                    onDelete()
                } else {
                    println("❌ Failed to delete favorite item: ${deleteResponse.status}")
                }
            } else {
                println("⚠️ No matching favorite item found for productId: $productId")
            }
        } catch (e: Exception) {
            println("❌ Error deleting favorite item: ${e.message}")
        } finally {
            client.close()
        }
    }


    suspend fun isFavorite(userId: String, productId: String): Boolean {
        return try {
            // Query Firebase to get items matching the productId
            val queryUrl = "${FirebaseUtil.BASE_URL}favourite_master.json?orderBy=\"userId\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body() // Fetch response as String

            // Parse JSON manually
            val responseJson = Json.parseToJsonElement(responseString).jsonObject

            // Check if any entry has the matching userId
            responseJson.values.any { jsonElement ->
                val favoriteItem = jsonElement.jsonObject
                favoriteItem["productId"]?.jsonPrimitive?.content == productId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun addFavorite(product: FavoriteProductMaster) {
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

            val response: HttpResponse = client.post("${FirebaseUtil.BASE_URL}favourite_master.json") {
                contentType(ContentType.Application.Json)
                setBody(product)
            }

            // Extract the unique key (favId) from Firebase response
            val responseBody = response.body<Map<String, String>>()
            val favId = responseBody["name"] ?: return // "name" contains the generated key

            // Update the favorite entry with the generated favId
            client.patch("${FirebaseUtil.BASE_URL}favourite_master/$favId.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("favId" to favId))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
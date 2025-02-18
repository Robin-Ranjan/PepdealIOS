package com.pepdeal.infotech.shopVideo

import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shopVideo.favShopVideo.FavouriteShopVideo
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
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ShopVideosRepo {
//    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin){
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    fun fetchShopVideoWithShopDetailsFlow(): Flow<ShopVideoWithShopDetail> = flow {
        try {
            // Step 1: Fetch all shop videos where flag = "0"
            val shopVideosResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shops_videos_master.json?orderBy=\"flag\"&equalTo=\"0\"")
            val shopVideosMap = shopVideosResponse.body<Map<String, ShopVideosMaster>>()

            val shopVideos = shopVideosMap.values.filter { it.isActive == "0" }

            // Step 2: Fetch shop details for each shopId
            for (shopVideo in shopVideos) {
                val shopDetailsResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master/${shopVideo.shopId}.json")

                if (shopDetailsResponse.status == HttpStatusCode.OK) {
                    val shopDetails = shopDetailsResponse.body<ShopMaster>()

                    // Step 3: Check shop conditions and emit the final result
//                    if (shopDetails.isActive == "0" && shopDetails.flag == "0") {
                        emit(ShopVideoWithShopDetail(shopVideo, shopDetails))
//                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

    suspend fun isSavedVideo(userId: String, shopId: String): Boolean {
        return try {
            // Query Firebase to get items matching the productId
            val queryUrl = "${FirebaseUtil.BASE_URL}favourite_shop_video_master.json?orderBy=\"user_id\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body() // Fetch response as String

            // Parse JSON manually
            println(userId)
            val responseJson = Json.parseToJsonElement(responseString).jsonObject
            println(responseJson)
            // Check if any entry has the matching userId
            responseJson.values.any { jsonElement ->
                val favoriteItem = jsonElement.jsonObject
                favoriteItem["shop_id"]?.jsonPrimitive?.content == shopId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addSaveVideo(shopVideo: FavouriteShopVideo) {
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

            val response: HttpResponse = client.post("${FirebaseUtil.BASE_URL}favourite_shop_video_master.json") {
                contentType(ContentType.Application.Json)
                setBody(shopVideo)
            }

            // Extract the unique key (favId) from Firebase response
            val responseBody = response.body<Map<String, String>>()
            val favouriteShopVideoId = responseBody["name"] ?: return // "name" contains the generated key

            // Update the favorite entry with the generated favId
            client.patch("${FirebaseUtil.BASE_URL}favourite_shop_video_master/$favouriteShopVideoId.json") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("favouriteShopVideoId" to favouriteShopVideoId))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeFavoriteItem(userId: String, shopId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin)
        try {
            // 1️⃣ Query Firebase to get all items for the user
            val queryUrl = "${FirebaseUtil.BASE_URL}favourite_shop_video_master.json?orderBy=\"user_id\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body()  // Fetch as String

            // 2️⃣ Parse JSON manually
            val responseJson = Json.parseToJsonElement(responseString).jsonObject

            // 3️⃣ Find the correct favId by filtering on productId
            val favouriteShopVideoId = responseJson.entries.firstOrNull { (_, jsonElement) ->
                jsonElement.jsonObject["shop_id"]?.jsonPrimitive?.content == shopId
            }?.key

            if (favouriteShopVideoId != null) {
                // 4️⃣ Delete the specific item using favId
                val deleteUrl = "${FirebaseUtil.BASE_URL}favourite_shop_video_master/$favouriteShopVideoId.json"
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
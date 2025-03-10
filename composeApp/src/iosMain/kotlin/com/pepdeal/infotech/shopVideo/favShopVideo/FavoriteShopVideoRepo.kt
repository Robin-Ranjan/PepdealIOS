package com.pepdeal.infotech.shopVideo.favShopVideo

import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shopVideo.ShopVideoWithShopDetail
import com.pepdeal.infotech.shopVideo.ShopVideosMaster
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class FavoriteShopVideoRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation)
    }

    private suspend fun fetchFavoriteShopVideo(userId: String): List<FavouriteShopVideo> {
        return try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}favourite_shop_video_master.json") {
                    parameter("orderBy", "\"user_id\"")
                    parameter("equalTo", "\"$userId\"")
                    contentType(ContentType.Application.Json)
                }

            if (response.status == HttpStatusCode.OK) {
                val favMap: Map<String, FavouriteShopVideo> =
                    json.decodeFromString(response.bodyAsText())
                favMap.values.toList()
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            emptyList()
        }
    }

    fun getFavoriteShopVideoForUserFlow(userId: String): Flow<ShopVideoWithShopDetail?> = channelFlow {
        try {
            val favouriteShopVideos = fetchFavoriteShopVideo(userId)
                .sortedByDescending { it.createdAt.toLongOrNull() ?: 0L }

            if(favouriteShopVideos.isEmpty()){
                send(null)
                return@channelFlow
            }

            favouriteShopVideos.forEach { favoriteVideo ->
                launch {
                    try {
                        val json = Json { ignoreUnknownKeys = true }

                        // Fetch shop details
                        val shopDetailsResponse: HttpResponse =
                            client.get("${FirebaseUtil.BASE_URL}shop_master/${favoriteVideo.shop_id}.json")

                        val shopDetails = if (shopDetailsResponse.status == HttpStatusCode.OK) {
                            val responseBody = shopDetailsResponse.bodyAsText()
                            if (responseBody.isNotBlank() && responseBody != "null") {
                                json.decodeFromString<ShopMaster>(responseBody)
                            } else null
                        } else null

                        // Fetch shop videos
                        val shopVideoResponse: HttpResponse =
                            client.get("${FirebaseUtil.BASE_URL}shops_videos_master/${favoriteVideo.shop_id}.json")

                        val shopVideo = if (shopVideoResponse.status == HttpStatusCode.OK) {
                            val responseBody = shopVideoResponse.bodyAsText()
                            if (responseBody.isNotBlank() && responseBody != "null") {
                                json.decodeFromString<ShopVideosMaster>(responseBody)
                            } else null
                        } else null

                        if (shopDetails != null && shopVideo != null) {
                            send(ShopVideoWithShopDetail(shopVideo, shopDetails))
                        }
                    } catch (e: Exception) {
                        println("Error fetching shop data: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Flow Error: ${e.message}")
            send(null)
        }
    }.flowOn(Dispatchers.IO)


}
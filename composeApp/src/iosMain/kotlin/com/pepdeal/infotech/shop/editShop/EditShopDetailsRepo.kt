package com.pepdeal.infotech.shop.editShop

import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class EditShopDetailsRepo {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json
        }
    }

    suspend fun fetchShopServices(shopId: String): ShopStatusMaster = withContext(Dispatchers.IO) {
        return@withContext try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}shop_status_master.json") {
                    parameter("orderBy", "\"shopId\"")
                    parameter("equalTo", "\"$shopId\"")
                    contentType(ContentType.Application.Json)
                }

            if (response.status == HttpStatusCode.OK) {
                val shopStatusMap: Map<String, ShopStatusMaster> =
                    json.decodeFromString(response.bodyAsText())
                shopStatusMap.values.firstOrNull() ?: ShopStatusMaster()
            } else {
                ShopStatusMaster()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ShopStatusMaster()
        }
    }

    suspend fun updateShopDetails(shopMaster: ShopMaster): Boolean {
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            // Fetch existing shop service entry by shopId
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
                    parameter("orderBy", "\"shopId\"")
                    parameter("equalTo", "\"${shopMaster.shopId}\"")
                    contentType(ContentType.Application.Json)
                }

            val existingData: Map<String, ShopMaster> =
                if (response.status == HttpStatusCode.OK) Json.decodeFromString(response.bodyAsText()) else emptyMap()

            val existingEntry =
                existingData.entries.firstOrNull { it.value.userId == shopMaster.userId }
            return if (existingEntry != null) {
                // If entry exists, update it
                val existingShopStatusId = existingEntry.key
                val updates = mapOf(
                    "shopDescription" to shopMaster.shopDescription,
                    "bgColourId" to shopMaster.bgColourId,
                    "fontStyleId" to shopMaster.fontStyleId,
                    "fontColourId" to shopMaster.fontColourId,
                    "showNumber" to shopMaster.showNumber,
                    "updatedAt" to shopMaster.updatedAt
                )

                val updateResponse: HttpResponse =
                    client.patch("${FirebaseUtil.BASE_URL}shop_master/$existingShopStatusId.json") {
                        contentType(ContentType.Application.Json)
                        setBody(updates)
                    }

                updateResponse.status.isSuccess()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            false
        }

    }


    suspend fun insertOrUpdateShopServices(shopStatus: ShopStatusMaster): Boolean {
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            // Fetch existing shop service entry by shopId
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}shop_status_master.json") {
                    parameter("orderBy", "\"shopId\"")
                    parameter("equalTo", "\"${shopStatus.shopId}\"")
                    contentType(ContentType.Application.Json)
                }

            val existingData: Map<String, ShopStatusMaster> =
                if (response.status == HttpStatusCode.OK) Json.decodeFromString(response.bodyAsText()) else emptyMap()

            val existingEntry =
                existingData.entries.firstOrNull { it.value.userId == shopStatus.userId }

            return if (existingEntry != null) {
                // If entry exists, update it
                val existingShopStatusId = existingEntry.key
                val updates = mapOf(
                    "cashOnDelivery" to shopStatus.cashOnDelivery,
                    "doorStep" to shopStatus.doorStep,
                    "homeDelivery" to shopStatus.homeDelivery,
                    "liveDemo" to shopStatus.liveDemo,
                    "offers" to shopStatus.offers,
                    "bargain" to shopStatus.bargain,
                    "updatedAt" to shopStatus.updatedAt
                )

                val updateResponse: HttpResponse =
                    client.patch("${FirebaseUtil.BASE_URL}shop_status_master/$existingShopStatusId.json") {
                        contentType(ContentType.Application.Json)
                        setBody(updates)
                    }

                updateResponse.status.isSuccess()
            } else {
                // If not found, create a new entry using Firebase auto-generated key
                val createResponse: HttpResponse =
                    client.post("${FirebaseUtil.BASE_URL}shop_status_master.json") {
                        contentType(ContentType.Application.Json)
                        setBody(shopStatus.copy(shopStatusId = "")) // Temporarily send empty ID
                    }

                return if (createResponse.status.isSuccess()) {
                    val responseBody =
                        Json.decodeFromString<Map<String, String>>(createResponse.bodyAsText())
                    val newShopStatusId =
                        responseBody.keys.firstOrNull() // Firebase auto-generated ID

                    if (newShopStatusId != null) {
                        // Update the entry with the correct shopStatusId
                        val updateResponse: HttpResponse =
                            client.patch("${FirebaseUtil.BASE_URL}shop_status_master/$newShopStatusId.json") {
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("shopStatusId" to newShopStatusId))
                            }

                        updateResponse.status.isSuccess()
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
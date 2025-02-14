package com.pepdeal.infotech.tickets

import UserMaster
import com.pepdeal.infotech.ProductImageMaster
import com.pepdeal.infotech.ProductMaster
import com.pepdeal.infotech.ShopMaster
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

class TicketRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin)

    fun getTicketForCustomerFlow(userId: String): Flow<ProductTicket> = flow {
        val customerTickets = fetchCustomerTicket(userId).sortedByDescending {
            it.updatedAt.toLongOrNull()?: 0L
        }
//        val userDetails = fetchUserDetails(userId)

        for (ticket in customerTickets) {
            val product = fetchProductDetails(ticket.productId) ?: continue

            if (product.isActive == "0" && product.flag == "0") {
                val shop = fetchShopDetails(product.shopId)

                if (shop?.flag == "0" && shop.isActive == "0") {
                    val image = fetchProductImage(product.productId)
                    if (image != null) {
                        emit(ProductTicket(ticket, imageUrl = image.productImages, userDetails = UserMaster(), productName =product.productName , mrp = product.mrp, sellingPrice = product.sellingPrice, onCall = product.onCall))
                    }
                }
            }
        }
    }

    private suspend fun fetchCustomerTicket(userId: String): List<TicketMaster> {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}ticket_master.json") {
                parameter("orderBy", "\"userId\"")
                parameter("equalTo", "\"$userId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val favMap: Map<String, TicketMaster> = json.decodeFromString(response.bodyAsText())
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
//            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master/$shopId.json") {
//            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
//                parameter("orderBy","\"shopId\"")
//                parameter("equalTo","\"$shopId\"")
//                contentType(ContentType.Application.Json)
//            }
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

    private suspend fun fetchUserDetails(userId: String): UserMaster {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}user_master.json") {
                parameter("orderBy", "\"userId\"")
                parameter("equalTo", "\"$userId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val userMap: Map<String, UserMaster> = json.decodeFromString(response.bodyAsText())
                return userMap.values.firstOrNull() ?: UserMaster() // ✅ Return first matching user
            }
            UserMaster() // ✅ Return null when no data is found
        } catch (e: Exception) {
            e.printStackTrace()
            UserMaster()
        }
    }
}
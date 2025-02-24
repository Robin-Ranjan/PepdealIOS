package com.pepdeal.infotech.tickets

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.shop.modal.ShopMaster
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
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SellerTicketRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin){
        install(ContentNegotiation){
            json(json)
        }
    }

    fun getTicketForSellerFlow(shopId: String): Flow<ProductTicket> = flow {
        val customerTickets = fetchSellerTicket(shopId)

        for (ticket in customerTickets) {
            val product = fetchProductDetails(ticket.productId) ?: continue

            if (product.isActive == "0" && product.flag == "0") {
                val shop = fetchShopDetails(product.shopId)

                if (shop?.flag == "0" && shop.isActive == "0") {
                    val image = fetchProductImage(product.productId)
                    val userDetails = fetchUserDetails(ticket.userId)

                    if (image != null) {
                        emit(
                            ProductTicket(
                                ticket,
                                imageUrl = image.productImages,
                                userDetails = userDetails,
                                productName = product.productName,
                                mrp = product.mrp,
                                sellingPrice = product.sellingPrice,
                                onCall = product.onCall
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchSellerTicket(shopId: String): List<TicketMaster> {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}ticket_master.json") {
                parameter("orderBy", "\"shopId\"")
                parameter("equalTo", "\"$shopId\"")
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

    suspend fun updateTicketMasterStatus(
        ticketId: String,
        status: String,
        onSuccess: (Boolean) -> Unit
    ) {
//        withContext(Dispatchers.IO) {
            try {
                val updateData = mapOf(
                    "ticketStatus" to status,
                    "updatedAt" to Util.getCurrentTimeStamp()
                )
                val response: HttpResponse =
                    client.patch("${FirebaseUtil.BASE_URL}ticket_master/$ticketId.json") {
                        contentType(ContentType.Application.Json)
                        setBody(updateData)
                    }

                onSuccess(response.status.isSuccess())
            } catch (e: Exception) {
                onSuccess(false)
            }
//        }
    }

}
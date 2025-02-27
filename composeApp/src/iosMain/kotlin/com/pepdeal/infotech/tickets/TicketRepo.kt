package com.pepdeal.infotech.tickets

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TicketRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin){
        install(ContentNegotiation){
            json(json)
        }
    }

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

    // Function to check if a ticket exists using Firebase REST API & Ktor
    suspend fun checkTicketExists(
        shopId: String,
        productId: String,
        userId: String
    ): Boolean {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}ticket_master.json") {
                parameter("orderBy", "\"userId\"")
                parameter("equalTo", "\"$userId\"")
                contentType(ContentType.Application.Json)
            }

            println("userId:$userId , productId:$productId , shopId:$shopId")

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.body()
                val ticketData: Map<String, TicketMaster> = Json.decodeFromString(responseBody)

                println(responseBody)

                // Check for matching ticket conditions
                ticketData.values.any { ticket ->
                    ticket.productId == productId &&
                            ticket.shopId == shopId &&
                            (ticket.ticketStatus == "0" || ticket.ticketStatus == "2")
                }
            } else {
                println("Error: Firebase responded with status ${response.status}")
                false
            }
        } catch (e: Exception) {
            println("Error fetching tickets: ${e.message}")
            false
        }
    }

    suspend fun addTicket(userMobileNo:String,ticketMaster: TicketMaster): Pair<Boolean, String> {
        return try {
//            val userMobileNo = getUserMobile(ticketMaster.userId)
            val shopMobileNo = getShopMobile(ticketMaster.shopId)
                ?: return false to "Failed to fetch user/shop details."

            if (userMobileNo == shopMobileNo) {
                return false to "You can't raise a ticket on your own product."
            }

//            // Step 4: Check if a ticket already exists
//            val existingTicket = checkExistingTicket(ticketMaster.productId, ticketMaster.userId, ticketMaster.shopId)
//            if (existingTicket != null) {
//                return if (existingTicket.ticketStatus == "3") {
//                    addNewTicket(ticketMaster)
//                } else {
//                    false to "Ticket already exists and is not delivered. Cannot add a new one."
//                }
//            }

            // Step 5: No existing ticket found, add a new one
            addNewTicket(ticketMaster)

        } catch (e: Exception) {
            println(e.message)
            false to "Error: ${e.message}"
        }
    }

//    private suspend fun getUserMobile(userId: String): String? {
//        return try {
//            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}/user_master/$userId/mobileNo.json")
//            response.body<String?>()
//        } catch (e: Exception) {
//            null
//        }
//    }

    private suspend fun getShopMobile(shopId: String): String? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}/shop_master/$shopId/shopMobileNo.json")
            response.body<String?>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun addNewTicket(ticketMaster: TicketMaster): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Create a new ticket node (Firebase auto-generates a unique key)
                val postResponse: HttpResponse = client.post("${FirebaseUtil.BASE_URL}ticket_master.json") {
                    contentType(ContentType.Application.Json)
                    setBody(ticketMaster)
                }

                // Extract the generated key from Firebase
                val responseBody = postResponse.bodyAsText()
                val generatedKey = extractFirebaseKey(responseBody)
                    ?: return@withContext false to "Failed to retrieve Firebase key."

                // Step 2: Update the ticket with its generated key
                val updatedTicket = ticketMaster.copy(ticketId = generatedKey)
                val patchResponse: HttpResponse = client.patch("${FirebaseUtil.BASE_URL}ticket_master/$generatedKey.json") {
                    contentType(ContentType.Application.Json)
                    setBody(updatedTicket)
                }

                if (patchResponse.status.isSuccess()) {
                    true to "Ticket added successfully with ID: $generatedKey"
                } else {
                    false to "Failed to update the ticket."
                }
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
                false to "Error: ${e.message}"
            }
        }
    }

    @Serializable
    data class FirebasePostResponse(val name: String)

    private fun extractFirebaseKey(responseBody: String): String? {
        return try {
            println(Json.decodeFromString<FirebasePostResponse>(responseBody).name)
            Json.decodeFromString<FirebasePostResponse>(responseBody).name
        } catch (e: Exception) {
            null
        }
    }

}
package com.pepdeal.infotech.tickets.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseRequest
import com.pepdeal.infotech.core.databaseUtils.DatabaseResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.FirestoreOperator
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.shop.repository.ShopRepository
import com.pepdeal.infotech.tickets.domain.TicketRepository
import com.pepdeal.infotech.tickets.model.ProductTicket
import com.pepdeal.infotech.tickets.model.TicketMaster
import com.pepdeal.infotech.user.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TicketRepositoryImpl(
    private val httpClient: HttpClient,
    private val productRepo: ProductRepository,
    private val shopRepo: ShopRepository,
    private val userRepo: UserRepository
) : TicketRepository {

    override suspend fun getTicketForCustomerFlow(userId: String): Flow<AppResult<ProductTicket, DataError.Remote>> =
        flow {

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.TICKET_MASTER,
                filters = listOf(FirestoreFilter("userId", userId))
            )


            val ticketResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> =
                safeCall {
                    httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(queryBody)
                    }
                }

            if (ticketResponse is AppResult.Error) {
                emit(AppResult.Error(ticketResponse.error))
                return@flow
            }

            val tickets = (ticketResponse as AppResult.Success).data.mapNotNull { doc ->
                val fields = doc.document?.fields ?: return@mapNotNull null
                TicketMaster(
                    ticketId = doc.document.name.substringAfterLast("/"),
                    userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    ticketStatus = (fields["ticketStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    sellingPrice = (fields["sellingPrice"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    colour = (fields["colour"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    sizeName = (fields["sizeName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    quantity = (fields["quantity"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                )
            }.sortedByDescending { it.updatedAt.toLongOrNull() ?: 0L }

            if (tickets.isEmpty()) {
                emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                return@flow
            }

            val userResult = userRepo.fetchUserDetails(userId)
            if (userResult is AppResult.Error) {
                emit(AppResult.Error(userResult.error))
                return@flow
            }
            val user = (userResult as AppResult.Success).data

            for (ticket in tickets) {
                val productResult = productRepo.fetchProductDetails(ticket.productId)
                if (productResult is AppResult.Error) continue
                val product = (productResult as AppResult.Success).data ?: continue
                val imageResult = productRepo.fetchProductImages(product.productId)
                val image = if (imageResult is AppResult.Success) imageResult.data else null

                emit(
                    AppResult.Success(
                        ProductTicket(
                            ticket = ticket,
                            userDetails = user,
                            productName = product.productName,
                            mrp = product.mrp,
                            sellingPrice = product.sellingPrice,
                            imageUrl = image?.firstOrNull()?.productImages ?: "",
                            onCall = product.onCall
                        )
                    )
                )
            }
        }

    // Function to check if a ticket exists using Firebase REST API & Ktor
    override suspend fun checkTicketExists(
        shopId: String,
        productId: String,
        userId: String
    ): AppResult<String, DataError.Remote> {
        val queryBody = buildFirestoreQuery(
            collection = DatabaseCollection.TICKET_MASTER,
            limit = 1,
            filters = listOf(
                FirestoreFilter("userId", userId),
                FirestoreFilter("productId", productId),
                FirestoreFilter("shopId", shopId),
                FirestoreFilter("ticketStatus", listOf("0", "2"), op = FirestoreOperator.IN)
            )
        )

        return safeCall {
            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }

            val responseBody = response.bodyAsText()
            val parsedResult: List<DatabaseQueryResponse> =
                AppJson.decodeFromString(responseBody)

            val matchingTicket = parsedResult.find { res ->
                val fields = res.document?.fields ?: return@find false
                val status = (fields["ticketStatus"] as? DatabaseValue.StringValue)?.stringValue
                status == "0" || status == "2"
            }

            return if (matchingTicket != null) {
                val status =
                    (matchingTicket.document?.fields?.get("ticketStatus") as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                AppResult.Success(status)
            } else {
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.NOT_FOUND,
                        message = "Ticket is already registred and not delivered"
                    )
                )
            }
        }
    }


    override suspend fun addTicket(
        userMobileNo: String,
        ticketMaster: TicketMaster
    ): AppResult<Unit, DataError.Remote> {
        return try {
            val shopMobileNo = shopRepo.getShopMobile(ticketMaster.shopId)
                ?: return AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.UNKNOWN,
                        message = "Failed to fetch shop mobile number."
                    )
                )

            if (userMobileNo == shopMobileNo) {
                return AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.UNKNOWN,
                        message = "You can't raise a ticket on your own product."
                    )
                )
            }

            when (val ticketCheckResult = checkTicketExists(
                ticketMaster.shopId,
                ticketMaster.productId,
                ticketMaster.userId
            )) {
                is AppResult.Success -> {
                    val status = ticketCheckResult.data
                    return if (status == "3") {
                        // No active ticket found, safe to add
                        when (val addResult = addNewTicket(ticketMaster)) {
                            is AppResult.Success -> AppResult.Success(Unit)
                            is AppResult.Error -> addResult
                        }
                    } else {
                        AppResult.Error(
                            DataError.Remote(
                                type = DataError.RemoteType.UNKNOWN,
                                message = "Ticket already exists and is not delivered. Cannot add a new one."
                            )
                        )
                    }
                }

                is AppResult.Error -> {
                    // Ticket check failed, but allow creating ticket as fallback
                    when (val addResult = addNewTicket(ticketMaster)) {
                        is AppResult.Success -> AppResult.Success(Unit)
                        is AppResult.Error -> addResult
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.UNKNOWN,
                    message = "Unexpected error occurred while adding ticket: ${e.message}"
                )
            )
        }
    }

    private suspend fun addNewTicket(ticketMaster: TicketMaster): AppResult<TicketMaster, DataError.Remote> {
        return try {
            val response: HttpResponse =
                httpClient.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.TICKET_MASTER}") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "userId" to DatabaseValue.StringValue(ticketMaster.userId),
                                "productId" to DatabaseValue.StringValue(ticketMaster.productId),
                                "shopId" to DatabaseValue.StringValue(ticketMaster.shopId),
                                "ticketStatus" to DatabaseValue.StringValue(ticketMaster.ticketStatus),
                                "sellingPrice" to DatabaseValue.StringValue(ticketMaster.sellingPrice),
                                "colour" to DatabaseValue.StringValue(ticketMaster.colour),
                                "sizeName" to DatabaseValue.StringValue(ticketMaster.sizeName),
                                "quantity" to DatabaseValue.StringValue(ticketMaster.quantity),
                                "createdAt" to DatabaseValue.StringValue(ticketMaster.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(ticketMaster.updatedAt)
                            )
                        )
                    )
                }

            val firestoreResponse: DatabaseResponse = response.body()
            val generatedId = firestoreResponse.name.substringAfterLast("/")

            // Step 2: Patch the document with its own ticketId
            val patchResponse: HttpResponse = httpClient.patch(
                "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.TICKET_MASTER}/$generatedId?updateMask.fieldPaths=ticketId"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "ticketId" to DatabaseValue.StringValue(generatedId)
                        )
                    )
                )
            }

            if (patchResponse.status.isSuccess()) {
                AppResult.Success(ticketMaster.copy(ticketId = generatedId))
            } else {
                throw IllegalStateException("Failed to patch ticket with ID")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.NOT_FOUND,
                    message = "Failed to add ticket"
                )
            )
        }

    }
}
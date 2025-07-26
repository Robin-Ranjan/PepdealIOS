package com.pepdeal.infotech.tickets.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestorePatchUrl
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.tickets.domain.SellerTicketRepository
import com.pepdeal.infotech.tickets.model.ProductTicket
import com.pepdeal.infotech.tickets.model.TicketMaster
import com.pepdeal.infotech.user.repository.UserRepository
import com.pepdeal.infotech.util.Util.getCurrentTimeStamp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SellerTicketRepositoryImpl(
    private val httpClient: HttpClient,
    private val productRepo: ProductRepository,
    private val userRepo: UserRepository
) : SellerTicketRepository {
    override suspend fun getTicketForSellerFlow(shopId: String): Flow<AppResult<ProductTicket, DataError.Remote>> =
        flow {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.TICKET_MASTER,
                filters = listOf(FirestoreFilter("shopId", shopId))
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

            for (ticket in tickets) {
                val productResult = productRepo.fetchProductDetails(ticket.productId)
                if (productResult is AppResult.Error) continue
                val product = (productResult as AppResult.Success).data ?: continue

                val imageResult = productRepo.fetchProductImages(product.productId)
                val image = if (imageResult is AppResult.Success) imageResult.data else null

                val userResult = userRepo.fetchUserDetails(ticket.userId)
                if (userResult is AppResult.Error) continue
                val user = (userResult as AppResult.Success).data

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

    override suspend fun updateTicketMasterStatus(
        tickerId: String,
        status: String
    ): Flow<AppResult<Unit, DataError.Remote>> = flow {
        try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_MASTER,
                filters = listOf(FirestoreFilter("ticketId", value = tickerId))
            )

            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }

            val responseBody = response.body<String>()

            println("response Body:- $responseBody")
            if (response.status.isSuccess()) {
                // Parse response as a list
                val databaseResponses: List<DatabaseQueryResponse> =
                    AppJson.decodeFromString(responseBody)

                val documents = databaseResponses.mapNotNull { it.document }
                if (databaseResponses.isNotEmpty()) {
                    val document = documents.first()
                    val generatedId = document.name.substringAfterLast("/")

                    val updatesPass = mapOf(
                        "ticketStatus" to status,
                        "updatedAt" to getCurrentTimeStamp()
                    )
                    val patchUrl = buildFirestorePatchUrl(
                        collection = DatabaseCollection.TICKET_MASTER,
                        documentId = generatedId,
                        fields = updatesPass.keys.toList()
                    )

                    val patchResponse = httpClient.patch(patchUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(updatesPass)
                    }

                    if (patchResponse.status == HttpStatusCode.OK) {
                        emit(AppResult.Success(Unit))
                    } else {
                        emit(
                            AppResult.Error(
                                DataError.Remote(
                                    type = DataError.RemoteType.SERVER,
                                    message = "Failed to update the status, Try again."
                                )
                            )
                        )
                    }
                } else {
                    emit(
                        AppResult.Error(
                            DataError.Remote(
                                type = DataError.RemoteType.SERVER,
                                message = "ticket Not found"
                            )
                        )
                    )
                }
            } else {
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            message = "Error in checking ticket"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            emit(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        message = e.message ?: "Something went wrong"
                    )
                )
            )
        }
    }
}
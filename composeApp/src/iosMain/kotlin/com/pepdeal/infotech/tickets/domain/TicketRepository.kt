package com.pepdeal.infotech.tickets.domain

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.tickets.model.ProductTicket
import com.pepdeal.infotech.tickets.model.TicketMaster
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    suspend fun getTicketForCustomerFlow(userId: String): Flow<AppResult<ProductTicket, DataError.Remote>>
    suspend fun checkTicketExists(
        shopId: String,
        productId: String,
        userId: String
    ): AppResult<String, DataError.Remote>

    suspend fun addTicket(
        userMobileNo: String,
        ticketMaster: TicketMaster
    ): AppResult<Unit, DataError.Remote>
}
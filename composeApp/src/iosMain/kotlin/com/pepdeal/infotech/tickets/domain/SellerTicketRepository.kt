package com.pepdeal.infotech.tickets.domain

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.tickets.model.ProductTicket
import kotlinx.coroutines.flow.Flow

interface SellerTicketRepository {
    suspend fun getTicketForSellerFlow(shopId: String): Flow<AppResult<ProductTicket, DataError.Remote>>
    suspend fun updateTicketMasterStatus(
        tickerId: String,
        status: String
    ): Flow<AppResult<Unit, DataError.Remote>>
}
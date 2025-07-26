package com.pepdeal.infotech.tickets.model

import com.pepdeal.infotech.user.UserMaster
import kotlinx.serialization.Serializable

@Serializable
data class TicketMaster (
    val ticketId:String = "",
    val userId:String = "",
    val productId:String = "",
    val shopId:String = "",
    var ticketStatus:String = "", // 0 = approved,1 = rejected, 2 = waiting 3= delivered
    val sellingPrice:String = "",
    val colour:String = "",
    val sizeName:String = "",
    val quantity:String = "",
    val createdAt:String = "",
    val updatedAt:String = ""
)

@Serializable
data class ProductTicket(
    val ticket: TicketMaster,
    val userDetails: UserMaster? = null,
    val productName: String,
    val mrp: String,
    val sellingPrice: String,
    val imageUrl: String,
    val onCall:String
)
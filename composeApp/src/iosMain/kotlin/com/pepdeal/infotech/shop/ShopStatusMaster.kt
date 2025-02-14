package com.pepdeal.infotech.shop

import kotlinx.serialization.Serializable

@Serializable
data class ShopStatusMaster(
    val shopStatusId: String = "",
    val userId: String = "",
    val shopId: String = "",
    val cashOnDelivery: String = "", // 0->yes , 1-> no
    val doorStep: String = "", // 0->yes , 1-> no
    val homeDelivery: String = "", // 0->yes , 1-> no
    val liveDemo: String = "", // 0->yes , 1-> no
    val offers: String = "", // 0->yes , 1-> no
    val bargain: String = "", // 0->yes , 1-> no
    val createdAt: String = "", // 0->yes , 1-> no
    val updatedAt: String = "" // 0->yes , 1-> no
)

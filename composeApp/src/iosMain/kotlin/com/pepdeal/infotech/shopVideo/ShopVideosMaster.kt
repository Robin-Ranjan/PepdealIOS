package com.pepdeal.infotech.shopVideo

import kotlinx.serialization.Serializable

@Serializable
data class ShopVideosMaster(
    val shopId:String = "",
    val videoUrl:String= "",
    val thumbNailUrl:String="",
    val flag:String ="",
    var isActive:String = "",
    var shopVideoId:String="",
    val createdAt:String = "",
    val updatedAt:String= ""
)

data class ValidationResult(val isValid: Boolean, val message: String)

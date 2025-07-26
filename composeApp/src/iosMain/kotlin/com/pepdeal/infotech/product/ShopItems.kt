package com.pepdeal.infotech.product

import kotlinx.serialization.Serializable

@Serializable
data class ShopItems(
    var productId: String = "",
    var shopId: String = "",
    val productName: String = "",
    val sellingPrice: String = "",
    val mrp: String = "",
    var image: String = "",
    val description: String = "",
    val category: String = "",
    val discountMrp: String = "",
    var productActive: String = "",
    var flag: String = "",
    var subCategoryId: String = "",
    val searchTag: List<String> = emptyList(),
    val onCall: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val shopActive: String = "",
    val shopBlock: String = "",
    val shopLongitude: String = "",
    val shopLatitude: String = "",
)

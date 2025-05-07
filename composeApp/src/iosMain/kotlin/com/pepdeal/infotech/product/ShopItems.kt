package com.pepdeal.infotech.product

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
    var isActive:String = "",
    var flag:String = "",
    var subCategoryId :String = "",
    val searchTag: String = "",
    val onCall:String = "",
    val createdAt:String = "",
    val updatedAt:String = "",
    val isShopActive: String = "",
    val isShopBlock: String = "",
    val shopLongitude: String = "",
    val shopLatitude: String = "",
)

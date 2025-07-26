package com.pepdeal.infotech.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductMaster(
    val productId: String = "",
    val userId: String = "",
    val shopId: String = "",
    val productName: String = "",
    val brandId: String = "",
    val brandName: String = "",
    val categoryId: String = "",
    val subCategoryId: String = "",
    val description: String = "",
    val description2: String = "",
    val specification: String = "",
    val warranty: String = "",
    val sizeId: String = "",
    val sizeName: String = "",
    val color: String = "",
    val searchTag: List<String> = emptyList(),
    val onCall: String = "", // 0-> Yes , 1-> No
    val mrp: String = "",
    val discountMrp: String = "",
    val sellingPrice: String = "",

    var productActive: String = "", //0->Active, 1->Block by the shop Owner
    val flag: String = "", // 0->Active, 1-> Block by the admin Control Panel

    val shopActive: String = "",
    val shopBlock: String = "",

    val shopLongitude: String = "",
    val shopLatitude: String = "",

    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class ProductImageMaster(
    val id: String = "",
    val productId: String = "",
    val productImages: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class ProductWithImages(
    val product: ProductMaster,
    val images: List<ProductImageMaster>
)

@Serializable
data class ProductUiDto(
    val shopItem: ShopItems,
    val isFavourite: Boolean
)

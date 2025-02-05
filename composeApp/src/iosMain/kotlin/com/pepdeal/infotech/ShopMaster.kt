package com.pepdeal.infotech

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class ShopMaster(
    val shopId:String? = null,
    val userId:String? = null,
    val shopName:String? = null,
    val shopMobileNo:String? = null,
    val shopAddress:String? = null,
    val shopAddress2:String? = null,
    val shopArea:String? = null,
    val city:String? = null,
    val state:String? = null,
    val shopDescription:String? = null,
    val bgColourId:String? = null,
    val fontSizeId:String? = null,
    val fontStyleId:String? = null,
    val fontColourId:String  = "",
   // @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive:String? = null, // 1 -> shop is not live or closed, 0 -> shop is open and live
    val flag:String? = null, //1 -> not verified by control panel  0-> verified by control panel
    val latitude:String? = null,
    val shopStatus:String? = null, //1 -> deleted , 0 -> not deleted
    val longitude:String? = null,
    val searchTag:String? = null,
   // @get:PropertyName("isVerified") @set:PropertyName("isVerified")
    var isVerified:String? = null, //1-> not verified  0-> verified
    val createdAt:String? = null,
    val updatedAt:String?= null,
    var showNumber :String = ""
)


@Serializable
data class ShopWithProducts(
    val shop: ShopMaster,
    val products: List<ProductWithImages>
)

@Serializable
data class ProductWithImages(
    val product: ProductMaster,
    val images: List<ProductImageMaster>
)

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
    val searchTag: String = "",
    val onCall: String = "", // 0-> Yes , 1-> No
    val mrp: String = "",
    val discountMrp: String = "",
    val sellingPrice: String = "",
    var isActive: String = "", //0->Active, 1->Block by the shop Owner
    val flag: String = "", // 0->Active, 1-> Block by the admin Control Panel
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class ProductImageMaster(
    val id:String = "",
    val productId: String = "",
    val productImages: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)


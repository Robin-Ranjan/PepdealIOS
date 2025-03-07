package com.pepdeal.infotech.shop.modal

import com.pepdeal.infotech.product.ProductWithImages
import kotlinx.serialization.Serializable

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

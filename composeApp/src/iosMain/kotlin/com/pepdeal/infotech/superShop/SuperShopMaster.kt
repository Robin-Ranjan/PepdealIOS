package com.pepdeal.infotech.superShop

import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import kotlinx.serialization.Serializable

@Serializable
data class SuperShopMaster(
    val superId:String = "",
    val userId:String = "",
    val shopId:String = "",
    val createdAt:String = "",
    val updatedAt:String = ""
)

data class SuperShopsWithProduct(
    val shop: ShopMaster,
    val products: List<ProductWithImages>,
    val createdAt :String
)
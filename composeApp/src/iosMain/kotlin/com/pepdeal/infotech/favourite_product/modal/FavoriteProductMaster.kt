package com.pepdeal.infotech.favourite_product.modal

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteProductMaster(
    var favId:String = "",
    var userId:String = "",
    var productId :String = "",
    var createdAt:String = "",
    var updatedAt:String= ""
)

@Serializable
data class FavProductWithImages(
    val product: ProductMaster,
    val images: List<ProductImageMaster>,
    val createdAt: String = ""
)

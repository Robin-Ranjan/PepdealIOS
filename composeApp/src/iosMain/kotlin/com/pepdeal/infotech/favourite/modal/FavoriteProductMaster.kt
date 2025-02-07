package com.pepdeal.infotech.favourite.modal

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteProductMaster(
    var favId:String = "",
    var userId:String = "",
    var productId :String = "",
    var createdAt:String = "",
    var updatedAt:String= ""
)

package com.pepdeal.infotech.shopVideo

import com.pepdeal.infotech.shop.modal.ShopMaster
import kotlinx.serialization.Serializable

@Serializable
data class ShopVideoWithShopDetail(
    val shopVideosMaster: ShopVideosMaster,
    val shopsMaster: ShopMaster
)

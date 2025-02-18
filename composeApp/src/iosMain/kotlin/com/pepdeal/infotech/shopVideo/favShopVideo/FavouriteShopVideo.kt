package com.pepdeal.infotech.shopVideo.favShopVideo

import kotlinx.serialization.Serializable


@Serializable
data class FavouriteShopVideo(
    val shop_id: String = "",
    val user_id: String = "",
    val shop_video_id: String = "",
    val favouriteShopVideoId: String = "",
    val createdAt: String = ""
)
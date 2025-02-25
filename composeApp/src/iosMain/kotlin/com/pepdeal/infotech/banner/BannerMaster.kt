package com.pepdeal.infotech.banner

import kotlinx.serialization.Serializable

@Serializable
data class BannerMaster(
    val bannerId: String,
    val bannerImage: String,
    val bannerName: String,
    val isActive: String,
    val bannerDescription: String,
    val updatedAt: String,
    val createdAt:String
)

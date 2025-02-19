package com.pepdeal.infotech

import kotlinx.serialization.Serializable

@Serializable
data class UserProfilePicMaster(
    val id:String = "",
    val userId:String = "",
    val profilePicUrl:String = "",
    val createdAt:String = "",
    val updatedAt:String = ""
)

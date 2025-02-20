package com.pepdeal.infotech

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirebaseUploadResponse (
    val name: String,
    val bucket: String,
    val generation: String,
    @SerialName("metageneration")
    val metaGeneration: String,
    val contentType: String,
    val timeCreated: String,
    val updated: String,
    val size: String,
    @SerialName("downloadTokens")
    val downloadTokens: String? = null
)
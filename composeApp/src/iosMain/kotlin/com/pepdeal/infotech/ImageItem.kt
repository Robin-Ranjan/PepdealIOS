package com.pepdeal.infotech

data class ImageItem(
    var imageId: String? = null,
    var url: String? = null
)

data class ShopServices(
    var isAvailable: Boolean = false,
    var serviceName: String? = null
)

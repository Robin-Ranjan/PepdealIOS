package com.pepdeal.infotech.navigation.routes

import kotlinx.serialization.Serializable

sealed class SubGraph{
    @Serializable
    object Auth:SubGraph()

    @Serializable
    object MainPage :SubGraph()
}

sealed class Routes {
    @Serializable
    object MainPage :Routes()

    @Serializable
    object LoginPage :Routes()

    @Serializable
    object RegistrationPage:Routes()

    @Serializable
    object OpenYourShopPage:Routes()

    @Serializable
    object ColorBottomSheetPage:Routes()

    companion object {
        const val ColorBottomSheet = "color_bottom_sheet"
        const val FontBottomSheet = "font_bottom_sheet"
    }
}
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
    object ListProductPage:Routes()

    @Serializable
    object FavouritesPage:Routes()

    companion object {
        const val ColorBottomSheet = "color_bottom_sheet"
        const val MultiColorBottomSheet = "multi_color_bottom_sheet"
        const val FontBottomSheet = "font_bottom_sheet"
        const val ProductCategoriesBottomSheet = "product_categories_bottom_sheet"
        const val ProductSubCategoriesBottomSheet = "product_subcategories_bottom_sheet"
    }
}
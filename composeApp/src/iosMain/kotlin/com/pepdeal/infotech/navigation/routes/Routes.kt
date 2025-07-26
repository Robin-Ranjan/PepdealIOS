package com.pepdeal.infotech.navigation.routes

import kotlinx.serialization.Serializable

sealed class SubGraph {
    @Serializable
    object Auth : SubGraph()

    @Serializable
    object MainPage : SubGraph()
}

sealed class Routes {
    @Serializable
    object MainPage : Routes()

    @Serializable
    object LoginPage : Routes()

    @Serializable
    object RegistrationPage : Routes()

    @Serializable
    object ForgetPasswordPage : Routes()

    @Serializable
    data class OpenYourShopPage(val shopPhoneNo: String, val userId: String) : Routes()

    @Serializable
    data class AddNewProductPage(val shopId: String) : Routes()

    @Serializable
    data class UpdateProductPage(val productId: String) : Routes()

    @Serializable
    data class FavouritesProductRoute(val userId: String) : Routes()

    @Serializable
    data class CustomerTicketPage(val userId: String) : Routes()

    @Serializable
    data class SellerTicketPage(val shopId: String) : Routes()

    @Serializable
    data class ShopDetails(val shopId: String, val userId: String) : Routes()

    @Serializable
    data class EditShopDetails(val shopId: String, val userId: String) : Routes()

    @Serializable
    data class PersonalInfoPage(val userId: String) : Routes()

    @Serializable
    data class SuperShopScreenRoute(val userId: String) : Routes()

    @Serializable
    data class FavoriteShopVideosPage(val userId: String) : Routes()

    @Serializable
    data class UploadShopVideoPage(val shopId: String) : Routes()

    @Serializable
    data class ListAllProductPage(val shopId: String) : Routes()

    @Serializable
    data class CategoryWiseProductPage(val subCategoryName: String) : Routes()

    @Serializable
    data class ProductDetailsPage(val productId: String) : Routes()

    @Serializable
    object SplashScreenPage : Routes()

    @Serializable
    data class YourShopScreenPage(val shopId: String) : Routes()

    @Serializable
    data class SupportScreenPage(val userName: String, val userMobileNo: String) : Routes()

    @Serializable
    object AboutUs : Routes()

    @Serializable
    data object AddressSearchRoute : Routes()

    @Serializable
    data object ShopScreenRoute : Routes()

    companion object {
        const val ColorBottomSheet = "color_bottom_sheet"
        const val EditShopColorBottomSheet = "edit_shop_color_bottom_sheet"
        const val MultiColorBottomSheet = "multi_color_bottom_sheet"
        const val FontBottomSheet = "font_bottom_sheet"
        const val EditShopFontBottomSheet = "edit_shop_font_bottom_sheet"
        const val ProductCategoriesBottomSheet = "product_categories_bottom_sheet"
        const val ProductSubCategoriesBottomSheet = "product_subcategories_bottom_sheet"
    }
}
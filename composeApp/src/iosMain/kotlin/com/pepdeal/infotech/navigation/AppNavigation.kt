package com.pepdeal.infotech.navigation

import com.pepdeal.infotech.MainBottomNavigationWithPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pepdeal.infotech.categories.CategoriesBottomSheet
import com.pepdeal.infotech.categories.SubCategoriesProductBottomSheet
import com.pepdeal.infotech.categoriesProduct.CategoryWiseProductScreen
import com.pepdeal.infotech.color.ColorBottomSheet
import com.pepdeal.infotech.color.MultipleColorBottomSheet
import com.pepdeal.infotech.favourite.FavoriteProductScreen
import com.pepdeal.infotech.fonts.FontBottomSheet
import com.pepdeal.infotech.login.LoginScreen
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.navigation.routes.SubGraph
import com.pepdeal.infotech.product.addProduct.AddNewProductScreen
import com.pepdeal.infotech.product.ListAllProductScreen
import com.pepdeal.infotech.product.producrDetails.ProductDetailScreen
import com.pepdeal.infotech.product.updateProduct.UpdateProductScreen
import com.pepdeal.infotech.registration.RegisterScreen
import com.pepdeal.infotech.shop.editShop.EditShopColorBottomSheet
import com.pepdeal.infotech.shop.editShop.EditShopDetailsScreen
import com.pepdeal.infotech.shop.editShop.EditShopFontBottomSheet
import com.pepdeal.infotech.shop.OpenYourShopScreen
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsWithProductPage
import com.pepdeal.infotech.shopVideo.UploadShopVideoScreen
import com.pepdeal.infotech.shopVideo.enableBackGestureForNavigationController
import com.pepdeal.infotech.shopVideo.favShopVideo.FavoriteShopVideoScreen
import com.pepdeal.infotech.superShop.SuperShopScreen
import com.pepdeal.infotech.tickets.CustomerTicketScreen
import com.pepdeal.infotech.tickets.SellerTicketScreen
import com.pepdeal.infotech.user.PersonalInfoScreen
import com.pepdeal.infotech.util.NavigationProvider


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavigationProvider.navController = navController

//    if (Platform.OS == OsFamily.IOS) {
    enableBackGestureForNavigationController()
//    }
    NavHost(navController = navController, startDestination = SubGraph.MainPage) {

        navigation<SubGraph.Auth>(startDestination = Routes.LoginPage) {
            composable<Routes.LoginPage> {
                LoginScreen(onLoginClick = {
                    navController.navigate(Routes.MainPage) {
                        popUpTo<Routes.LoginPage> {
                            inclusive = true
                        }
                    }
                },
                    onForgotPasswordClick = {

                    })
            }

            composable<Routes.RegistrationPage> {
                RegisterScreen()
            }
        }

        navigation<SubGraph.MainPage>(startDestination = Routes.MainPage) {
            composable<Routes.MainPage> {
                MainBottomNavigationWithPager()
            }

            composable<Routes.OpenYourShopPage> {
                OpenYourShopScreen()
            }

            composable<Routes.AddNewProductPage> {
                AddNewProductScreen()
            }

            composable<Routes.FavouritesPage> {
                val userId = it.toRoute<Routes.FavouritesPage>().userId
                FavoriteProductScreen(userId)
            }

            composable<Routes.CustomerTicketPage> {
                val userId = it.toRoute<Routes.CustomerTicketPage>().userId
                CustomerTicketScreen(userId)
            }

            composable<Routes.SellerTicketPage> {
                SellerTicketScreen()
            }

            composable<Routes.ShopDetails> { backStackEntry ->
                val shopId = backStackEntry.toRoute<Routes.ShopDetails>().shopId
                val userId = backStackEntry.toRoute<Routes.ShopDetails>().userId
                ShopDetailsWithProductPage(shopId, userId)
            }

            composable<Routes.EditShopDetails> {
                val shopId = it.toRoute<Routes.EditShopDetails>().shopId
                val userId = it.toRoute<Routes.EditShopDetails>().userId
                EditShopDetailsScreen(shopId, userId)
            }

            composable<Routes.PersonalInfoPage> {
                val userId = it.toRoute<Routes.PersonalInfoPage>().userId
                PersonalInfoScreen(userId)
            }

            composable<Routes.SuperShopPage> {
                val userId = it.toRoute<Routes.SuperShopPage>().userId
                SuperShopScreen(userId)
            }

            composable<Routes.FavoriteShopVideosPage> {
                val userId = it.toRoute<Routes.FavoriteShopVideosPage>().userId
                FavoriteShopVideoScreen(userId)
            }

            composable<Routes.UploadShopVideoPage> {
                val shopId = it.toRoute<Routes.UploadShopVideoPage>().shopId
                UploadShopVideoScreen(shopId)
            }

            composable<Routes.UpdateProductPage> {
                val productId = it.toRoute<Routes.UpdateProductPage>().productId
                UpdateProductScreen(productId)
            }

            composable<Routes.ListAllProductPage> {
                val shopId = it.toRoute<Routes.ListAllProductPage>().shopId
                ListAllProductScreen(shopId)
            }

            composable<Routes.CategoryWiseProductPage> {
                val subCategoryName = it.toRoute<Routes.CategoryWiseProductPage>().subCategoryName
                CategoryWiseProductScreen(subCategoryName)
            }

            composable<Routes.ProductDetailsPage> {
                val productId = it.toRoute<Routes.ProductDetailsPage>().productId
                ProductDetailScreen(productId)
            }
            dialog(
                route = Routes.ColorBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true // Optional, for full-width bottom sheet
                )
            ) {  // Use `dialog` for BottomSheet navigation
                ColorBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }

            dialog(
                route = Routes.MultiColorBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true // Optional, for full-width bottom sheet
                )
            ) {  // Use `dialog` for BottomSheet navigation
                MultipleColorBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }

            dialog(
                route = Routes.FontBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true,
                )
            ) {
                FontBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }

            dialog(
                route = Routes.ProductCategoriesBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true,
                )
            ) {
                CategoriesBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }

            dialog(
                route = Routes.ProductSubCategoriesBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true,
                )
            ) {
                SubCategoriesProductBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }

            dialog(
                route = Routes.EditShopColorBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true,
                )
            ) {
                EditShopColorBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }

            dialog(
                route = Routes.EditShopFontBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true,
                )
            ) {
                EditShopFontBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }
        }

    }
}
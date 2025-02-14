package com.pepdeal.infotech.navigation

import MainBottomNavigationWithPager
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
import com.pepdeal.infotech.color.ColorBottomSheet
import com.pepdeal.infotech.color.MultipleColorBottomSheet
import com.pepdeal.infotech.favourite.FavoriteProductScreen
import com.pepdeal.infotech.fonts.FontBottomSheet
import com.pepdeal.infotech.login.LoginScreen
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.navigation.routes.SubGraph
import com.pepdeal.infotech.product.ListProductScreen
import com.pepdeal.infotech.registration.RegisterScreen
import com.pepdeal.infotech.shop.EditShopDetailsScreen
import com.pepdeal.infotech.shop.OpenYourShopScreen
import com.pepdeal.infotech.shop.ShopDetailsWithProductPage
import com.pepdeal.infotech.superShop.SuperShopScreen
import com.pepdeal.infotech.tickets.CustomerTicketScreen
import com.pepdeal.infotech.tickets.SellerTicketScreen
import com.pepdeal.infotech.user.PersonalInfoScreen
import com.pepdeal.infotech.util.NavigationProvider


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavigationProvider.navController = navController
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

                    }, onRegisterClick = {
//                        navController.navigate(Routes.RegistrationPage) {
//                            popUpTo<Routes.LoginPage> {
//                                inclusive = true
//                            }
//                        }
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

            composable<Routes.ListProductPage> {
                ListProductScreen()
            }

            composable<Routes.FavouritesPage> {
                FavoriteProductScreen()
            }

            composable<Routes.CustomerTicketPage> {
                CustomerTicketScreen()
            }

            composable<Routes.SellerTicketPage> {
                SellerTicketScreen()
            }

            composable<Routes.ShopDetails> { backStackEntry ->
                val shopId = backStackEntry.toRoute<Routes.ShopDetails>().shopId
                val userId = backStackEntry.toRoute<Routes.ShopDetails>().userId
                ShopDetailsWithProductPage(shopId,userId)
            }

            composable<Routes.EditShopDetails> {
                val shopId = it.toRoute<Routes.EditShopDetails>().shopId
                EditShopDetailsScreen(shopId)
            }

            composable<Routes.PersonalInfoPage> {
                val userId = it.toRoute<Routes.PersonalInfoPage>().userId
                PersonalInfoScreen(userId)
            }

            composable<Routes.SuperShopPage> {
                val userId = it.toRoute<Routes.SuperShopPage>().userId
                SuperShopScreen(userId)
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
        }

    }
}
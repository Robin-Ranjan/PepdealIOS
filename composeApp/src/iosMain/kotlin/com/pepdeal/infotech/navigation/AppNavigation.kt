package com.pepdeal.infotech.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pepdeal.infotech.AboutUsScreen
import com.pepdeal.infotech.ForgetPassScreen
import com.pepdeal.infotech.MainBottomNavigationWithPager
import com.pepdeal.infotech.SplashScreen
import com.pepdeal.infotech.categories.CategoriesBottomSheet
import com.pepdeal.infotech.categories.SubCategoriesProductBottomSheet
import com.pepdeal.infotech.categoriesProduct.screen.CategoryWiseProductScreenRoot
import com.pepdeal.infotech.color.ColorBottomSheet
import com.pepdeal.infotech.color.MultipleColorBottomSheet
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.favourite_product.screen.FavoriteProductScreenRoot
import com.pepdeal.infotech.fonts.FontBottomSheet
import com.pepdeal.infotech.login.screen.LoginScreenRoot
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.navigation.routes.SubGraph
import com.pepdeal.infotech.placeAPI.screen.SearchAddressRoot
import com.pepdeal.infotech.product.ProductViewModel
import com.pepdeal.infotech.product.addProduct.AddNewProductScreen
import com.pepdeal.infotech.product.listProduct.screen.ListAllProductScreenRoot
import com.pepdeal.infotech.product.producrDetails.ProductDetailScreen
import com.pepdeal.infotech.product.updateProduct.UpdateProductScreen
import com.pepdeal.infotech.registration.RegisterScreen
import com.pepdeal.infotech.shop.OpenYourShopScreen
import com.pepdeal.infotech.shop.editShop.EditShopColorBottomSheet
import com.pepdeal.infotech.shop.editShop.EditShopDetailsScreen
import com.pepdeal.infotech.shop.editShop.EditShopFontBottomSheet
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsWithProductPage
import com.pepdeal.infotech.shop.viewModel.AddressData
import com.pepdeal.infotech.shop.viewModel.ShopViewModel
import com.pepdeal.infotech.shopVideo.favShopVideo.screen.FavoriteShopVideoScreenRoot
import com.pepdeal.infotech.shopVideo.uploadShopVideo.UploadShopVideoScreen
import com.pepdeal.infotech.superShop.screen.SuperShopScreenRoot
import com.pepdeal.infotech.support.SupportScreen
import com.pepdeal.infotech.tickets.screen.CustomerTicketScreenRoot
import com.pepdeal.infotech.tickets.screen.SellerTicketScreenRoot
import com.pepdeal.infotech.user.PersonalInfoScreen
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.yourShop.YourShopScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavigationProvider.navController = navController

    NavHost(navController = navController, startDestination = SubGraph.MainPage) {

        navigation<SubGraph.Auth>(startDestination = Routes.LoginPage) {
            composable<Routes.LoginPage> {
                LoginScreenRoot()
            }

            composable<Routes.RegistrationPage> {
                RegisterScreen()
            }

            composable<Routes.ForgetPasswordPage> {
                ForgetPassScreen()
            }

        }

        navigation<SubGraph.MainPage>(startDestination = Routes.SplashScreenPage) {
            composable<Routes.MainPage> { backStackEntry ->
                println("→ About to call navigate with payload ")
                val shopViewModel = koinViewModel<ShopViewModel>()
                val productViewModel = koinViewModel<ProductViewModel>()

                LaunchedEffect(Unit) {
                    backStackEntry.savedStateHandle.get<String>("newAddressDeal")?.let { jsonData ->
                        val newAddress = AppJson.decodeFromString(
                            AddressData.serializer(), jsonData
                        )
                        shopViewModel.onAction(ShopViewModel.Action.OnLocationChange(newAddress))
                        productViewModel.onAction(ProductViewModel.Action.OnLocationChange(newAddress))
                        backStackEntry.savedStateHandle.remove<String>("newAddressDeal")
                    }
                }
                println("entered the screen main 1")
                MainBottomNavigationWithPager()
            }

            composable<Routes.OpenYourShopPage> {
                val shopPhoneNo = it.toRoute<Routes.OpenYourShopPage>().shopPhoneNo
                val userId = it.toRoute<Routes.OpenYourShopPage>().userId
                OpenYourShopScreen(shopPhoneNo, userId)
            }

            composable<Routes.AddNewProductPage> {
                val shopId = it.toRoute<Routes.AddNewProductPage>().shopId
                AddNewProductScreen(shopId)
            }

            composable<Routes.FavouritesProductRoute> {
                FavoriteProductScreenRoot()
            }

            composable<Routes.CustomerTicketPage> {
                CustomerTicketScreenRoot()
            }

            composable<Routes.SellerTicketPage> {
                SellerTicketScreenRoot()
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

            composable<Routes.SuperShopScreenRoute> {
                SuperShopScreenRoot()
            }

            composable<Routes.FavoriteShopVideosPage> {
                FavoriteShopVideoScreenRoot()
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
                ListAllProductScreenRoot()
            }

            composable<Routes.CategoryWiseProductPage> {
                CategoryWiseProductScreenRoot()
            }

            composable<Routes.ProductDetailsPage> {
                val productId = it.toRoute<Routes.ProductDetailsPage>().productId
                ProductDetailScreen(productId)
            }

            composable<Routes.SupportScreenPage> {
                val userName = it.toRoute<Routes.SupportScreenPage>().userName
                val mobileNo = it.toRoute<Routes.SupportScreenPage>().userMobileNo

                SupportScreen(userName, userPhoneNo = mobileNo)
            }

            composable<Routes.YourShopScreenPage> {
                val shopId = it.toRoute<Routes.YourShopScreenPage>().shopId
                YourShopScreen(shopId)
            }

            composable<Routes.AboutUs> {
                AboutUsScreen()
            }

            composable<Routes.SplashScreenPage> {
                SplashScreen()
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

            composable<Routes.AddressSearchRoute> {
                SearchAddressRoot()
            }
        }

    }
}
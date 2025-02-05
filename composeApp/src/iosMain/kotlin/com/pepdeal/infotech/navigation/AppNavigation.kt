package com.pepdeal.infotech.navigation

import MainBottomNavigationWithPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.pepdeal.infotech.color.ColorBottomSheet
import com.pepdeal.infotech.fonts.FontBottomSheet
import com.pepdeal.infotech.login.LoginScreen
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.navigation.routes.SubGraph
import com.pepdeal.infotech.registration.RegisterScreen
import com.pepdeal.infotech.shop.OpenYourShopScreen
import com.pepdeal.infotech.util.ColorUtil
import com.pepdeal.infotech.util.NavigationProvider

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavigationProvider.navController = navController
    NavHost(navController = navController, startDestination = SubGraph.MainPage) {

        navigation<SubGraph.Auth>(startDestination = Routes.LoginPage) {
            composable<Routes.LoginPage> {
                LoginScreen(onLoginClick = {
                    navController.navigate(Routes.MainPage){
                        popUpTo<Routes.LoginPage> {
                            inclusive = true
                        }
                    }
                },
                    onForgotPasswordClick = {

                    }, onRegisterClick = {
                        navController.navigate(Routes.RegistrationPage){
                            popUpTo<Routes.LoginPage> {
                                inclusive = true
                            }
                        }
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
            dialog(route = Routes.ColorBottomSheet,
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

            dialog(route = Routes.FontBottomSheet,
                dialogProperties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = true,
                )
            ){
                FontBottomSheet(onDismiss = {
                    navController.popBackStack()
                })
            }
        }

    }
}
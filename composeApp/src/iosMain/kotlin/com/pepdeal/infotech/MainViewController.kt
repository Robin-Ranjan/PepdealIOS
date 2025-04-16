package com.pepdeal.infotech

import androidx.compose.ui.window.ComposeUIViewController
import com.pepdeal.infotech.navigation.AppNavigation
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController(

    ) {
        AppNavigation()
    }
}
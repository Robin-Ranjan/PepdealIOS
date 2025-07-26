package com.pepdeal.infotech

import androidx.compose.ui.window.ComposeUIViewController
import com.pepdeal.infotech.di.platformModule
import com.pepdeal.infotech.di.sharedModule
import com.pepdeal.infotech.navigation.AppNavigation
import org.koin.compose.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        KoinApplication(
            application = {
                logger(PrintLogger(level = Level.DEBUG))
                // Test modules one by one
                modules(sharedModule, platformModule) // Comment out platformModule first
                // modules(platformModule) // Add this back after testing
            }
        ) {
            AppNavigation()

            println("module loaded successfully")
        }
    }
}
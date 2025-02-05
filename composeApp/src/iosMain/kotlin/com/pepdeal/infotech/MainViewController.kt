package com.pepdeal.infotech

import androidx.compose.ui.window.ComposeUIViewController
import com.pepdeal.infotech.navigation.AppNavigation

fun MainViewController() = ComposeUIViewController { AppNavigation() }
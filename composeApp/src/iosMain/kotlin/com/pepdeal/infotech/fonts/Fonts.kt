package com.pepdeal.infotech.fonts

import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.FontResource

data class Fonts(
    val name:String,
    val fontFamily:FontResource,
    var isSelected:Boolean = false
)
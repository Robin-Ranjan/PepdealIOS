package com.pepdeal.infotech.fonts

import org.jetbrains.compose.resources.FontResource

data class Fonts(
    val name:String,
    val fontFamily:FontResource,
    var isSelected:Boolean = false
)
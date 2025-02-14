package com.pepdeal.infotech.fonts

import pepdealios.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.FontResource
import pepdealios.composeapp.generated.resources.alkatra_bold
import pepdealios.composeapp.generated.resources.almendra_display
import pepdealios.composeapp.generated.resources.manrope
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.open_sans_extrabold
import pepdealios.composeapp.generated.resources.quicksand_bold
import pepdealios.composeapp.generated.resources.roboto_condensed_bold
import pepdealios.composeapp.generated.resources.yeseva_one

object FontUtils {
    val fontMap: Map<String, FontResource> = mapOf(
        "Open Sans Extra" to Res.font.open_sans_extrabold,
        "Quicksand" to Res.font.quicksand_bold,
        "Yeseva One" to Res.font.yeseva_one,
        "ManRope" to Res.font.manrope_bold,
        "Almendra Display" to Res.font.almendra_display,
        "Roboto" to Res.font.roboto_condensed_bold,
        "Alkatra" to Res.font.alkatra_bold
    )


    val fontList: List<Fonts> = fontMap.map { (name, font) ->
        Fonts(name, font)
    }

    fun getFontResourceByName(fontName: String): FontResource? {
        return try {
            fontMap[fontName]
        }catch (e:Exception){
            println(e.message)
            Res.font.manrope_medium
        }
    }
}
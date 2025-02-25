package com.pepdeal.infotech.util

import com.pepdeal.infotech.color.ColorItem

object ColorUtil {
    val colorMap: Map<String, String> = mapOf(
        "Red" to "#EF3A5D",
        "LIGHT CREAM" to "#EDE8D1",
        "Green" to "#00FF00",
        "WAX Green" to "#D7D98A",
        "BALTIC Green" to "#3AA3A0",
        "LIGHT GREEN" to "#6AC66B",
        "Blue" to "#0000FF",
        "CHARCOL" to "#303030",
        "Black" to "#000000",
        "White" to "#FFFFFF",
        "Light Orange" to "#FF8484",
        "Yellow" to "#FFFF00",
        "Cyan" to "#00FFFF",
        "Magenta" to "#FF00FF",
        "Gray" to "#808080",
        "Orange" to "#FFA500",
        "Pink" to "#FFC0CB",
        "LIGHT Pink" to "#F2C2C2",
        "Brown" to "#A52A2A",
        "Purple" to "#800080",
        "Navy" to "#000080",
        "Teal" to "#008080",
        "Lime" to "#00FF00",
        "Olive" to "#808000",
        "Coral" to "#FF7F50",
        "Salmon" to "#FA8072",
        "Gold" to "#FFD700",
        "Violet" to "#EE82EE",
        "SWEET LAVENDER" to "#989BBE",
        "Peach" to "#FFDAB9",
        "Mint" to "#98FF98",
        "Chocolate" to "#D2691E",
        "Khaki" to "#F0E68C",
        "SlateBlue" to "#6A5ACD",
        "DarkRed" to "#8B0000",
        "LightCoral" to "#F08080",
        "MediumPurple" to "#9370DB",
        "DarkCyan" to "#008B8B",
        "Indigo" to "#4B0082",
        "Peru" to "#CD853F",
        "LightGoldenRodYellow" to "#FAFAD2",
        "SteelBlue" to "#4682B4",
        "Sienna" to "#A0522D",
        "DarkSlateGray" to "#2F4F4F",
        "FireBrick" to "#B22222",
        "Wheat" to "#F5DEB3",
        "Sky Blue" to "#87CEEB",
        "Blue Ice Color" to "#17E5EB",
        "MintGreen" to "#98FF98",
        "Peach" to "#FFDAB9",
        "CherryRed" to "#FF4E50",
        "OceanBlue" to "#0077B6",
        "SunflowerYellow" to "#FFC300",
        "BlushPink" to "#FF6F91",
        "ForestGreen" to "#228B22",
        "MidnightBlue" to "#191970",
        "Turquoise" to "#40E0D0",
        "Amber" to "#FFBF00",
        "Sapphire" to "#0F52BA",
        "RoseGold" to "#B76E79",
        "SteelGray" to "#708090",
        "Ivory" to "#FFFFF0",
        "SeafoamGreen" to "#2E8B57",
        "ElectricPurple" to "#BF00FF",
        "Goldenrod" to "#DAA520",
        "Crimson" to "#DC143C",
        "ArcticBlue" to "#D0F0C0",
        "BurntOrange" to "#CC5500",
        "SlateBlue" to "#6A5ACD",
        "PearlWhite" to "#F8F8FF",
        "JetBlack" to "#343434",
        "MultiColour" to "#123456",
        "None" to "#1234"
    )

    val colorList: List<ColorItem> = colorMap.map { (name, hexCode) ->
        ColorItem(name, hexCode)
    }.distinctBy { it.hexCode }
}
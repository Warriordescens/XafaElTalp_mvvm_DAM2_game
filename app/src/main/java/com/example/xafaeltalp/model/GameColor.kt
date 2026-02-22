package com.example.xafaeltalp.model

import androidx.compose.ui.graphics.Color
import kotlin.collections.toList

enum class GameColor (val id: Int, val color: Color, val label:String){
    // 4 nivell facil
    RED(1, Color(red = 255, green = 0, blue = 0), "Vermell"),
    YELLOW(2, Color(red = 225, green = 255, blue = 0), "Groc"),
    GREEN(3, Color(red = 0, green = 255, blue = 100), "Verd"),
    BLUE(4, Color(red = 0, green = 120, blue = 225), "Blau"),

    // Fins a 9 mode difícil
    ORANGE(5, Color(red = 255, green = 165, blue = 0), "Taronja"),
    CYAN(6, Color(red = 0, green = 255, blue = 255), "Cian"),
    PINK(7, Color(red = 255, green = 0, blue = 200), "Rosa"),
    PURPLE(8, Color(red = 125, green = 0, blue = 255), "Lila"),
    LIME(9, Color(red = 160, green = 210, blue = 0), "Llima");

//    companion object{
//        fun getColorsForLevel(numColors: Int): List<GameColor> {
//            val llistaCompleta = values().toList()
//            return llistaCompleta.subList(0, numColors)
//        }
//    }

    companion object{
        fun getColorsForLevel(numColors: Int) = values().take(numColors)
    }
}


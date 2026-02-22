package com.example.xafaeltalp.navigation

// Classe per centralitzar les possibles rutes i no tenir-les distribuides.
sealed class AppScreens (val route: String ){
    // Pantalla de Login: ruta simple
    data object Login : AppScreens("login_screen")


    data object Welcome : AppScreens( "welcome_screen") {
        fun createRoute() = "welcome_screen"
    }

    data object Game : AppScreens( "game_screen") {
        fun createRoute() = "game_screen"
    }

    // Aquesta pantalla no canvia cap dada, per tant no té paràmetres.

}
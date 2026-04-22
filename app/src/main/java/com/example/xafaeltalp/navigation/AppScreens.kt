package com.example.xafaeltalp.navigation

sealed class AppScreens (val route: String ){
    data object Login : AppScreens("login_screen")

    data object Welcome : AppScreens( "welcome_screen") {
        fun createRoute() = "welcome_screen"
    }

    data object ModeSelection : AppScreens("mode_selection_screen")

    data object Game : AppScreens( "game_screen/{mode}/{difficulty}") {
        fun createRoute(mode: String, difficulty: String = "normal") = "game_screen/$mode/$difficulty"
    }

    data object Info : AppScreens("info_screen")
}
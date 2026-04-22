package com.example.xafaeltalp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.xafaeltalp.view.ScreenLogin
import com.example.xafaeltalp.view.ScreenWelcome
import com.example.xafaeltalp.view.game.GameScreen
import com.example.xafaeltalp.viewmodel.GameViewmodel
import com.example.xafaeltalp.viewmodel.LoginViewModel
import com.example.xafaeltalp.view.ScreenInfo
import com.example.xafaeltalp.view.ModeSelectionScreen

fun setInclusiveTrue(builder: PopUpToBuilder) {
    builder.inclusive = true
}
fun configurarPopUpLogin(builder: NavOptionsBuilder) {
    builder.popUpTo(AppScreens.Login.route, ::setInclusiveTrue)
}

@Composable
fun AppNavigation(
    onCloseApp: () -> Unit,
    gameViewModel: GameViewmodel = viewModel()
){
    val navController = rememberNavController()

    fun ferLogout() = navController.navigate(AppScreens.Login.route, ::configurarPopUpLogin)
    fun anarASeleccioMode() = navController.navigate(AppScreens.ModeSelection.route)
    fun tornarEnrere() = navController.popBackStack()

    fun processarRutaViewModelLogin(route: String) {
        navController.navigate(route, ::configurarPopUpLogin)
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ){
        composable( route= AppScreens.Login.route ){
            val viewModel: LoginViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(key1 = true) {
                viewModel.navigationChannel.collect ( ::processarRutaViewModelLogin)
            }

            ScreenLogin(
                state = state,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onRegisterClick = viewModel::onRegisterClick,
                onLoginClick = viewModel::onLoginClick,
                onCloseClick = onCloseApp
            )
        }

        composable(route = AppScreens.Welcome.route) {
            ScreenWelcome(
                onLogoutClick = ::ferLogout,
                onStartGame = ::anarASeleccioMode,
                onInfoClick = { navController.navigate(AppScreens.Info.route) }
            )
        }

        composable(route = AppScreens.ModeSelection.route) {
            ModeSelectionScreen(
                onModeSelected = { mode, difficulty ->
                    navController.navigate(AppScreens.Game.createRoute(mode, difficulty))
                },
                onBack = ::tornarEnrere
            )
        }

        composable(
            route = AppScreens.Game.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType; defaultValue = "normal" }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "endless"
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "normal"
            
            GameScreen(
                vm = gameViewModel,
                mode = mode,
                difficulty = difficulty,
                onBackClick = ::tornarEnrere
            )
        }

        composable(route = AppScreens.Info.route) {
            ScreenInfo(onBackClick = ::tornarEnrere)
        }
    }
}

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

fun setInclusiveTrue(builder: PopUpToBuilder) {
    builder.inclusive = true
}
fun configurarPopUpLogin(builder: NavOptionsBuilder) {
    builder.popUpTo(AppScreens.Login.route, ::setInclusiveTrue)
}

fun configurarArgUsername(builder: androidx.navigation.NavArgumentBuilder) {
    builder.type = NavType.StringType
}

////////////////////////////////////////////////////////////////
@Composable
fun AppNavigation(
    onCloseApp: () -> Unit
){
    val navController = rememberNavController()
    val gameViewModel: GameViewmodel = viewModel()

    fun ferLogout() = navController.navigate(AppScreens.Login.route, ::configurarPopUpLogin)
    fun anarAJugar() = navController.navigate(AppScreens.Game.route)
    fun tornarEnrere() = navController.popBackStack()

    fun processarRutaViewModelLogin(route: String) {
        navController.navigate(route, ::configurarPopUpLogin)
    }

    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ){      // Aqui es defineixen les rutes
        // RUTA 1: Login
        composable( route= AppScreens.Login.route ){
            // instancia del viewModel de Login
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



        // RUTA 2 : WELCOME
        composable(
            route = AppScreens.Welcome.route,
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                    nullable = true           // Permite que el dato no venga
                    defaultValue = "Jugador"   // Valor por defecto si no hay login previo
                }
            )
        ) { backStackEntry ->
            // Extraemos el nombre de forma segura
            val username = backStackEntry.arguments?.getString("username") ?: "Jugador"

            ScreenWelcome(
                onLogoutClick = ::ferLogout,
                onStartGame = ::anarAJugar,
                // Asegúrate de pasar estas funciones si tu ScreenWelcome las pide ahora
                onRankingClick = { /* Próximamente */ },
                onConfigClick = { /* Próximamente */ }
            )
        }

        // RUTA 3 : La pantalla del Joc
        composable(route = AppScreens.Game.route) {
            GameScreen(
                vm = gameViewModel, // Pasamos el ViewModel
                onBackClick = ::tornarEnrere
            )
        }

//        composable(
//            route = AppScreens.Welcome.route,
//            arguments = listOf(navArgument("username", ::configurarArgUsername))
//        ) { backStackEntry ->
//            val username = backStackEntry.arguments?.getString("username") ?: "Desconegut"
//
//            ScreenWelcome(
//                onLogoutClick = ::ferLogout,
//                onStartGame = ::anarAJugar,
//                onRankingClick = {
//                    println("Navegar a Ranking para $username")
//                    // Aquí podrías hacer: navController.navigate(AppScreens.Ranking.route)
//                },
//                onConfigClick = {
//                    println("Abrir Configuración")
//                }
//            )
//        }
    }
}
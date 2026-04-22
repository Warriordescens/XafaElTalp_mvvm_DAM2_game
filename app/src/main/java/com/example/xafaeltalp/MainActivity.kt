package com.example.xafaeltalp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xafaeltalp.navigation.AppNavigation
import com.example.xafaeltalp.ui.theme.XafaElTalpTheme
import com.example.xafaeltalp.viewmodel.GameViewmodel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewmodel = viewModel()

            // Observador del ciclo de vida para pausar automáticamente
            DisposableEffect(Lifecycle.Event.ON_PAUSE) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        gameViewModel.pauseGame()
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            XafaElTalpTheme {
                AppNavigation(
                    onCloseApp = ::finalitzarAplicacio,
                    gameViewModel = gameViewModel
                )
            }
        }
    }

    private fun finalitzarAplicacio() {
        this.finish();
    }
}
package com.example.xafaeltalp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.xafaeltalp.navigation.AppNavigation
import com.example.xafaeltalp.ui.theme.XafaElTalpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XafaElTalpTheme {
                AppNavigation(
                    onCloseApp = ::finalitzarAplicacio
                )
            }
        }
    }

    private fun finalitzarAplicacio() {
        this.finish();
    }
}
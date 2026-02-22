package com.example.xafaeltalp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.xafaeltalp.R
import com.example.xafaeltalp.view.game.bounceClick

@Composable
fun ScreenWelcome(
    onLogoutClick: () -> Unit,
    onStartGame: () -> Unit,
    onRankingClick: () -> Unit = {},
    onConfigClick: () -> Unit = {}
) {
    val fondoColor = Color(0xFFF4E3B1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoColor)
            .padding(
                bottom = 100.dp,
                start = 0.dp,
                top = 60.dp,
                end = 0.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- FILA SUPERIOR: CONFIG Y RANKING ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.config_button),
                contentDescription = "Configuración",
                modifier = Modifier
                    .size(200.dp)
                    .bounceClick { onConfigClick() }
            )

            Image(
                painter = painterResource(id = R.drawable.ranking_button),
                contentDescription = "Ranking",
                modifier = Modifier
                    .size(205.dp)
                    .bounceClick { onRankingClick() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTÓN CENTRAL: PLAY ---
        Image(
            painter = painterResource(id = R.drawable.play_button),
            contentDescription = "Jugar",
            modifier = Modifier
                .size(400.dp) // Tamaño destacado
                .bounceClick { onStartGame() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTÓN INFERIOR: SALIR ---
        Button(
            onClick = { onLogoutClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8D6E63)
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(105.dp)
                .padding(top = 50.dp)
        ) {
            Text("SALIR", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
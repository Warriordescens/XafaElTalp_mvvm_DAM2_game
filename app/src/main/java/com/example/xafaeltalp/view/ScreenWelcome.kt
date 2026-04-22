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
import androidx.compose.ui.unit.sp
import com.example.xafaeltalp.R
import com.example.xafaeltalp.view.game.bounceClick

@Composable
fun ScreenWelcome(
    onLogoutClick: () -> Unit,
    onStartGame: () -> Unit,
    onRankingClick: () -> Unit = {},
    onConfigClick: () -> Unit = {},
    onInfoClick: () -> Unit
) {
    val fondoColor = Color(0xFFF4E3B1)

    Box(modifier = Modifier.fillMaxSize().background(fondoColor)) {

        // --- INFORMACIÓN ARRIBA A LA DERECHA ---
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 20.dp)
                .size(60.dp)
                .bounceClick { onInfoClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("ℹ️", fontSize = 35.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // --- PLAY EN EL CENTRO ---
            Image(
                painter = painterResource(id = R.drawable.play_button),
                contentDescription = "Jugar",
                modifier = Modifier.size(400.dp).bounceClick { onStartGame() }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- SALIR ---
            Button(
                onClick = { onLogoutClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                modifier = Modifier.fillMaxWidth(0.6f).height(65.dp)
            ) {
                Text("SALIR", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

package com.example.xafaeltalp.view.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xafaeltalp.R
import com.example.xafaeltalp.viewmodel.GameViewmodel

@Composable
fun GameScreen(
    vm: GameViewmodel,
    onBackClick: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val tierraOscura = Color(0xFF5D4037)

    LaunchedEffect(Unit) {
        vm.resetGame()
        vm.startGame()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. FONDO DE IMAGEN REAL DEL JUEGO ---
        Image(
            painter = painterResource(id = R.drawable.game_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Para que cubra toda la pantalla
        )

        // --- 2. UI DEL JUEGO ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Marcadores con estilo "Cartel"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreBadge(label = "PUNTOS", value = "${state.score}", color = Color(0xFF4CAF50))
                ScoreBadge(label = "TIEMPO", value = "${state.timeLeft}s", color = if (state.timeLeft < 10) Color.Red else tierraOscura)
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- TABLERO 3x3 ESTILO TIERRA ---
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.2f)) // Sombra para el tablero
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.size(340.dp),
                    userScrollEnabled = false
                ) {
                    items(9) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.White.copy(alpha = 0.15f)) // Hueco del talp
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(15.dp))
                                .bounceClick { vm.onMoleHit(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.moleIndex == index) {
                                Image(
                                    painter = painterResource(id = R.drawable.talp_button),
                                    contentDescription = "Talp",
                                    modifier = Modifier.fillMaxSize(0.9f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))
        }

        // --- 3. CAPA DE GAME OVER ---
        if (state.isGameOver) {
            GameOverOverlay(score = state.score, onRetry = {
                vm.resetGame()
                vm.startGame()
            }, onMenu = onBackClick)
        }
    }
}

@Composable
fun ScoreBadge(label: String, value: String, color: Color) {
    Surface(
        color = Color(0xFFF4E3B1),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun GameOverOverlay(score: Int, onRetry: () -> Unit, onMenu: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¡FI DEL TIEMPS!", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("Has aconseguit $score punts", color = Color.Yellow, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.7f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("REINTENTAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth(0.7f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("MENÚ PRINCIPAL", fontWeight = FontWeight.Bold)
            }
        }
    }
}
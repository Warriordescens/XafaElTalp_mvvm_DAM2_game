package com.example.xafaeltalp.view.game

import androidx.compose.animation.*
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
    mode: String,
    difficulty: String,
    onBackClick: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val tierraOscura = Color(0xFF5D4037)

    LaunchedEffect(key1 = mode, key2 = difficulty) {
        vm.resetGame()
        vm.startGame(mode, difficulty)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.game_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con info del modo
            Text(
                text = if (state.gameMode == "endless") "NIVEL ${state.level}" else "BOSS FIGHT (${state.difficulty.uppercase()})",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreBadge(label = "PUNTOS", value = "${state.score}/${state.targetScore}", color = Color(0xFF4CAF50))
                ScoreBadge(label = "TIEMPO", value = "${state.timeLeft}s", color = if (state.timeLeft < 10) Color.Red else tierraOscura)
            }

            // Mensaje del Boss
            Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                this@Column.AnimatedVisibility(
                    visible = state.bossActionMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = state.bossActionMessage ?: "",
                        color = Color.Yellow,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.size(340.dp),
                    userScrollEnabled = false
                ) {
                    items(9) { index ->
                        val isBlocked = state.blockedCells.contains(index)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .background(if (isBlocked) Color(0xFF3E2723) else Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(15.dp))
                                .bounceClick { vm.onMoleHit(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBlocked) {
                                Text("🪨", fontSize = 30.sp) // Tierra/Bloqueo
                            } else if (state.moleIndex == index) {
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

        if (state.isGameOver) {
            GameOverOverlay(score = state.score, onRetry = {
                vm.resetGame()
                vm.startGame(mode, difficulty)
            }, onMenu = onBackClick)
        }

        if (state.isLevelCleared) {
            VictoryOverlay(onNext = {
                if (mode == "endless") {
                    // La lógica de nextLevel ya está en el VM y se dispara por tiempo
                    // Pero si queremos un botón de "Siguiente" podemos añadirlo
                } else {
                    onBackClick() // Volver al menú tras ganar al boss
                }
            }, isEndless = mode == "endless")
        }
    }
}

@Composable
fun VictoryOverlay(onNext: () -> Unit, isEndless: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¡VICTORIA!", color = Color.Green, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onNext) {
                Text(if (isEndless) "SIGUIENTE NIVEL" else "VOLVER AL MENÚ")
            }
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
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
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
            Text("¡GAME OVER!", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("Puntuación: $score", color = Color.Yellow, fontSize = 24.sp)

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

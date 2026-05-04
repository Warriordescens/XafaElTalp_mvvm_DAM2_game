package com.example.xafaeltalp.view.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.xafaeltalp.R
import com.example.xafaeltalp.viewmodel.GameEvent
import com.example.xafaeltalp.viewmodel.GameViewmodel
import com.example.xafaeltalp.viewmodel.MoleType

@Composable
fun GameScreen(
    vm: GameViewmodel,
    mode: String,
    difficulty: String,
    onBackClick: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val tierraOscura = Color(0xFF5D4037)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Requisito: Auto-Pausa al salir de la App
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                vm.onEvent(GameEvent.PauseGame)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = mode, key2 = difficulty) {
        vm.onEvent(GameEvent.StartGame(mode, difficulty))
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = tierraOscura),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp).bounceClick { onBackClick() }
                ) {
                    Text("SALIR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = if (state.gameMode == "endless") "NIVEL ${state.level}" else "BOSS FIGHT",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (state.gameMode == "boss") {
                BossHealthBar(health = state.bossHealth)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ScoreBadge(
                        label = "ATAQUE JEFE", 
                        value = "${state.timeLeft}s", 
                        color = if (state.timeLeft < 4) Color.Red else tierraOscura
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Requisito: animateFloatAsState cuando el score cambia (feedback)
                    val scoreScale by animateFloatAsState(
                        targetValue = if (state.score > 0) 1.1f else 1f,
                        animationSpec = tween(durationMillis = 100),
                        label = "ScoreScale"
                    )
                    
                    Box(modifier = Modifier.scale(scoreScale)) {
                        ScoreBadge(label = "PUNTOS", value = "${state.score}/${state.targetScore}", color = Color(0xFF4CAF50))
                    }
                    
                    // Requisito: animateColorAsState para feedback del tiempo
                    val timerColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (state.timeLeft < 5) Color.Red else tierraOscura,
                        animationSpec = tween(durationMillis = 500),
                        label = "TimerColor"
                    )
                    
                    ScoreBadge(label = "TIEMPO", value = "${state.timeLeft}s", color = timerColor)
                }
            }

            Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                if (state.bossActionMessage != null) {
                    Text(
                        text = state.bossActionMessage ?: "",
                        color = Color.Yellow,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                } else if (state.isSlowed) {
                    Text(
                        text = "❄️ ¡RALENTIZADO! ❄️",
                        color = Color.Cyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    items(9) { index ->
                        val isBlocked = state.blockedCells.contains(index)
                        val moleType = state.activeMoles[index]
                        
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .background(if (isBlocked) Color(0xFF3E2723) else Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(15.dp))
                                .bounceClick { vm.onEvent(GameEvent.HitMole(index)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBlocked) {
                                Text("🪨", fontSize = 35.sp)
                            } else if (moleType != null) {
                                MoleIcon(type = moleType)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            if (state.gameMode == "boss") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    repeat(3) { i ->
                        val active = i < state.lives
                        Text(
                            text = "❤️",
                            fontSize = 35.sp,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .graphicsLayer(alpha = if (active) 1f else 0.3f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        if (state.isPaused) {
            PauseOverlay(onResume = { vm.onEvent(GameEvent.ResumeGame) }, onMenu = onBackClick)
        }

        if (state.isGameOver) {
            GameOverOverlay(score = state.score, onRetry = {
                vm.onEvent(GameEvent.ResetGame)
                vm.onEvent(GameEvent.StartGame(mode, difficulty))
            }, onMenu = onBackClick)
        }

        if (state.isLevelCleared) {
            VictoryOverlay(onNext = {
                if (mode == "endless") {
                } else {
                    onBackClick()
                }
            }, isEndless = mode == "endless")
        }
    }
}

@Composable
fun BossHealthBar(health: Float) {
    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        Text("VIDA DEL JEFE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(health)
                    .fillMaxHeight()
                    .background(
                        if (health > 0.4f) Color(0xFFF44336) else Color(0xFFFFEB3B)
                    )
            )
        }
    }
}

@Composable
fun PauseOverlay(onResume: () -> Unit, onMenu: () -> Unit) {
    val fondoPergamino = Color(0xFFF4E3B1)
    val tierraOscura = Color(0xFF5D4037)

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = fondoPergamino,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.8f).padding(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "JUEGO EN PAUSA", 
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.Black, 
                    color = tierraOscura
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(60.dp).bounceClick { onResume() },
                    colors = ButtonDefaults.buttonColors(containerColor = tierraOscura),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONTINUAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, tierraOscura),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SALIR AL MENÚ", color = tierraOscura, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MoleIcon(type: MoleType) {
    // Requisito: Animación de estado (Escalado al aparecer)
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = tween(durationMillis = 300),
        label = "MoleScale"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().scale(scale)) {
        val resId = when(type) {
            MoleType.NORMAL -> R.drawable.talp_button
            MoleType.GOLDEN -> R.drawable.goldentalp
            MoleType.LESS_TIME -> R.drawable.lesstimetalp
            MoleType.SLOWED -> R.drawable.slowedtalp
            MoleType.BOMB -> R.drawable.bomb
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.9f)
        )
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
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
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

fun Modifier.alpha(alpha: Float): Modifier = this.then(Modifier.graphicsLayer(alpha = alpha))

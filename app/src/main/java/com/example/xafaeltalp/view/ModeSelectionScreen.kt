package com.example.xafaeltalp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xafaeltalp.R
import com.example.xafaeltalp.view.game.bounceClick

@Composable
fun ModeSelectionScreen(
    onModeSelected: (mode: String, difficulty: String) -> Unit,
    onBack: () -> Unit
) {
    val fondoPergamino = Color(0xFFF4E3B1)
    val tierraOscura = Color(0xFF5D4037)
    val tierraClara = Color(0xFF8D6E63)
    var showDifficultyDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo de imagen para mantener la estética
        Image(
            painter = painterResource(id = R.drawable.game_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Capa de color semi-transparente para que se vea el texto
        Box(modifier = Modifier.fillMaxSize().background(fondoPergamino.copy(alpha = 0.85f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SELECCIONA MODO",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tierraOscura
            )
            
            Spacer(modifier = Modifier.height(60.dp))

            // Botón Estilo Juego: Endless
            Button(
                onClick = { onModeSelected("endless", "normal") },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(75.dp)
                    .bounceClick { onModeSelected("endless", "normal") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tierraOscura),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("MODO ENDLESS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Estilo Juego: Boss Fight
            Button(
                onClick = { showDifficultyDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(75.dp)
                    .bounceClick { showDifficultyDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tierraOscura),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("BOSS FIGHT", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Botón Volver
            TextButton(
                onClick = onBack,
                modifier = Modifier.bounceClick { onBack() }
            ) {
                Text("VOLVER AL MENÚ", color = tierraClara, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (showDifficultyDialog) {
            AlertDialog(
                onDismissRequest = { showDifficultyDialog = false },
                containerColor = fondoPergamino,
                shape = RoundedCornerShape(16.dp),
                title = { 
                    Text(
                        "DIFICULTAD", 
                        color = tierraOscura, 
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 24.sp
                    ) 
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DifficultyButton("FÁCIL", tierraClara) { onModeSelected("boss", "easy") }
                        DifficultyButton("NORMAL", tierraOscura) { onModeSelected("boss", "normal") }
                        DifficultyButton("DIFÍCIL", Color(0xFF3E2723)) { onModeSelected("boss", "hard") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDifficultyDialog = false }) {
                        Text("CANCELAR", color = tierraOscura)
                    }
                }
            )
        }
    }
}

@Composable
fun DifficultyButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .height(55.dp)
            .bounceClick { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

package com.example.xafaeltalp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeSelectionScreen(
    onModeSelected: (mode: String, difficulty: String) -> Unit,
    onBack: () -> Unit
) {
    val fondoColor = Color(0xFFF4E3B1)
    var showDifficultyDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(fondoColor).padding(20.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SELECCIONA MODO",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = { onModeSelected("endless", "normal") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A))
            ) {
                Text("MODO ENDLESS", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showDifficultyDialog = true },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("BOSS FIGHT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(onClick = onBack) {
                Text("VOLVER", color = Color(0xFF795548), fontSize = 18.sp)
            }
        }

        if (showDifficultyDialog) {
            AlertDialog(
                onDismissRequest = { showDifficultyDialog = false },
                title = { Text("Selecciona Dificultad") },
                text = {
                    Column {
                        DifficultyOption("Fácil", Color(0xFF4CAF50)) { onModeSelected("boss", "easy") }
                        DifficultyOption("Normal", Color(0xFFFFC107)) { onModeSelected("boss", "normal") }
                        DifficultyOption("Difícil", Color(0xFFF44336)) { onModeSelected("boss", "hard") }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDifficultyDialog = false }) {
                        Text("CANCELAR")
                    }
                }
            )
        }
    }
}

@Composable
fun DifficultyOption(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

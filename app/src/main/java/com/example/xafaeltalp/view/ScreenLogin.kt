package com.example.xafaeltalp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.xafaeltalp.viewmodel.LoginUiState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.example.xafaeltalp.view.game.bounceClick

// COM PINTO LA PANTALLA?
@Composable
fun ScreenLogin(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit
){
    // Paleta de colores coherente con el juego
    val tierraOscura = Color(0xFF5D4037)
    val tierraClara = Color(0xFF8D6E63)
    val fondoPergamino = Color(0xFFF4E3B1)
    val errorRojo = Color(0xFFBA1A1A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoPergamino)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- MENSAJE DE BIENVENIDA ESTILO BURBUJA ---
        if (state.message.isNotEmpty()) {
            Surface(
                color = tierraClara.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Text(
                    text = state.message,
                    color = tierraOscura,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = "XAFA EL TALP",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tierraOscura,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- CAMPOS DE TEXTO ESTILIZADOS ---
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tierraOscura,
                unfocusedBorderColor = tierraClara,
                focusedLabelColor = tierraOscura
            )
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tierraOscura,
                unfocusedBorderColor = tierraClara,
                focusedLabelColor = tierraOscura
            )
        )

        // --- BOTONES PRINCIPALES ---
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp).bounceClick { onLoginClick() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tierraOscura)
        ) {
            Text("ENTRAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = tierraOscura)
            ) {
                Text("REGISTRAR")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onCloseClick,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("CERRAR")
            }
        }

        // --- ERROR MENSAJE ---
        if (state.errorMsg.isNotEmpty()) {
            Text(
                text = state.errorMsg,
                color = errorRojo,
                modifier = Modifier.padding(top = 24.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



package com.example.xafaeltalp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xafaeltalp.view.game.bounceClick

@Composable
fun ScreenInfo(onBackClick: () -> Unit) {
    val fondoColor = Color(0xFFF4E3B1)
    val tierraOscura = Color(0xFF5D4037)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "INFORMACIÓ",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = tierraOscura
        )

        Spacer(modifier = Modifier.height(30.dp))

        Surface(
            color = Color.White.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Aquesta app ha estat desenvolupada per:",
                    fontSize = 18.sp,
                    color = tierraOscura,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Alex Hernández Guerrero",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD84315),
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(0.6f).height(55.dp).bounceClick { onBackClick() },
            colors = ButtonDefaults.buttonColors(containerColor = tierraOscura)
        ) {
            Text("TORNAR")
        }
    }
}
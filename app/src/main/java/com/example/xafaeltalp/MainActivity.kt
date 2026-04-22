package com.example.xafaeltalp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xafaeltalp.navigation.AppNavigation
import com.example.xafaeltalp.ui.theme.XafaElTalpTheme
import com.example.xafaeltalp.viewmodel.GameViewmodel

class MainActivity : ComponentActivity() {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var ultimTempsSacsejada: Long = 0

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                // Log para que veas en el Logcat qué valores da tu emulador al moverlo
                if (gForce > 1.1f) Log.d("SENSOR_TEST", "Fuerza G: $gForce")

                // Bajamos a 1.2f para que el emulador lo pille más fácil
                if (gForce > 1.2f) {
                    val tempsActual = System.currentTimeMillis()
                    if (tempsActual - ultimTempsSacsejada > 2000) {
                        ultimTempsSacsejada = tempsActual
                        Log.d("SENSOR", "¡SACSEJADA! Cerrando app...")
                        finalitzarAplicacio()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val gameViewModel: GameViewmodel = viewModel()

            DisposableEffect(Lifecycle.Event.ON_PAUSE) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        gameViewModel.pauseGame()
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            XafaElTalpTheme {
                AppNavigation(
                    onCloseApp = ::finalitzarAplicacio,
                    gameViewModel = gameViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activarSensor()
    }

    override fun onPause() {
        super.onPause()
        desactivarSensor()
    }

    private fun activarSensor() {
        if (sensorManager == null) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        accelerometer?.let {
            // SENSOR_DELAY_GAME es más rápido para no perder el pico de la sacudida
            sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun desactivarSensor() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    private fun finalitzarAplicacio() {
        // finishAffinity cierra todo el árbol de actividades de golpe
        this.finishAffinity()
    }
}

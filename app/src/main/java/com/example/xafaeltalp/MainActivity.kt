package com.example.xafaeltalp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xafaeltalp.navigation.AppNavigation
import com.example.xafaeltalp.ui.theme.XafaElTalpTheme
import com.example.xafaeltalp.viewmodel.GameEvent
import com.example.xafaeltalp.viewmodel.GameSound
import com.example.xafaeltalp.viewmodel.GameViewmodel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var ultimTempsSacsejada: Long = 0
    private var mediaPlayer: MediaPlayer? = null

    private var soundPool: SoundPool? = null
    private var soundHitId: Int = 0
    private var soundExplosionId: Int = 0
    private var soundGoldenHitId: Int = 0
    private var gameViewModel: GameViewmodel? = null

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

                // Log para que ver en el Logcat qué valores da
                if (gForce > 1.1f) Log.d("SENSOR_TEST", "Fuerza G: $gForce")

                // 1.2f para que el emulador lo pille más fácil
                if (gForce > 1.2f) {
                    val tempsActual = System.currentTimeMillis()
                    if (tempsActual - ultimTempsSacsejada > 2000) {
                        ultimTempsSacsejada = tempsActual
                        Log.d("SENSOR", "¡SACSEJADA! Enviando evento al ViewModel")
                        gameViewModel?.onEvent(GameEvent.ShakeDetected)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicialitzar música de fons
        Log.d("SOUND_DEBUG", "Inicializando MediaPlayer...")
        mediaPlayer = MediaPlayer.create(this, R.raw.musica_joc)
        if (mediaPlayer == null) {
            Log.e("SOUND_DEBUG", "¡ERROR! No se pudo crear el MediaPlayer")
        } else {
            val musicAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            mediaPlayer?.setAudioAttributes(musicAttributes)
            mediaPlayer?.isLooping = true
            Log.d("SOUND_DEBUG", "MediaPlayer inicializado correctamente")
        }

        // Inicialitzar SoundPool para efectos cortos
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundHitId = soundPool?.load(this, R.raw.xafar_talp_sound, 1) ?: 0
        soundExplosionId = soundPool?.load(this, R.raw.explosion, 1) ?: 0
        soundGoldenHitId = soundPool?.load(this, R.raw.gold_talp_sound, 1) ?: 0

        setContent {
            val vm: GameViewmodel = viewModel()
            gameViewModel = vm

            LaunchedEffect(Unit) {
                vm.soundEvents.collectLatest { sound ->
                    Log.d("SOUND_DEBUG", "Evento de sonido recibido: $sound")
                    when (sound) {
                        GameSound.HIT -> {
                            Log.d("SOUND_DEBUG", "Reproduciendo HIT")
                            soundPool?.play(soundHitId, 1f, 1f, 1, 0, 1f)
                        }
                        GameSound.EXPLOSION -> {
                            Log.d("SOUND_DEBUG", "Reproduciendo EXPLOSION")
                            soundPool?.play(soundExplosionId, 1f, 1f, 2, 0, 1f)
                        }
                        GameSound.GOLDEN_HIT -> {
                            Log.d("SOUND_DEBUG", "Reproduciendo GOLDEN_HIT")
                            soundPool?.play(soundGoldenHitId, 1f, 1f, 1, 0, 1f)
                        }
                    }
                }
            }

            DisposableEffect(Lifecycle.Event.ON_PAUSE) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        vm.onEvent(GameEvent.PauseGame)
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
                    gameViewModel = vm
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activarSensor()
        Log.d("SOUND_DEBUG", "Intentando reproducir música (start)")
        mediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        desactivarSensor()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool?.release()
        soundPool = null
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
        // finishAffinity cierra el árbol de actividades de golpe
        this.finishAffinity()
    }
}

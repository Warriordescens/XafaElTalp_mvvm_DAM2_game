package com.example.xafaeltalp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xafaeltalp.model.AppDatabase
import com.example.xafaeltalp.model.GameRecord
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

enum class MoleType {
    NORMAL, GOLDEN, LESS_TIME, SLOWED, BOMB
}

enum class GameSound {
    HIT, EXPLOSION, GOLDEN_HIT
}

sealed interface GameEvent {
    data class StartGame(val mode: String, val difficulty: String) : GameEvent
    data object PauseGame : GameEvent
    data object ResumeGame : GameEvent
    data class HitMole(val index: Int) : GameEvent
    data object ResetGame : GameEvent
    data object ShakeDetected : GameEvent
}

data class GameUiState(
    val score: Int = 0,
    val timeLeft: Int = 15, 
    val activeMoles: Map<Int, MoleType> = emptyMap(),
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val level: Int = 1,
    val bossHealth: Float = 1f, 
    val targetScore: Int = 50,
    val gameMode: String = "endless",
    val difficulty: String = "normal",
    val blockedCells: Set<Int> = emptySet(),
    val isLevelCleared: Boolean = false,
    val bossActionMessage: String? = null,
    val isSlowed: Boolean = false,
    val lives: Int = 3
)

class GameViewmodel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private val gameDao = AppDatabase.getDatabase(application).gameDao()

    private val _soundEvents = Channel<GameSound>(Channel.BUFFERED)
    val soundEvents = _soundEvents.receiveAsFlow()

    private var gameJob: Job? = null
    private var bossJob: Job? = null
    private var moleSpawnJob: Job? = null
    
    private var maxBossHealth = 200f

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.StartGame -> startGame(event.mode, event.difficulty)
            is GameEvent.PauseGame -> pauseGame()
            is GameEvent.ResumeGame -> resumeGame()
            is GameEvent.HitMole -> onMoleHit(event.index)
            is GameEvent.ResetGame -> resetGame()
            is GameEvent.ShakeDetected -> {
                if (!_uiState.value.isGameOver && !_uiState.value.isPaused) {
                    // Acción del Shake: Por ejemplo, limpiar topos actuales (ayuda al usuario)
                    _uiState.value = _uiState.value.copy(activeMoles = emptyMap())
                    _soundEvents.trySend(GameSound.EXPLOSION)
                }
            }
        }
    }

    private fun startGame(mode: String, difficulty: String) {
        val isFirstStart = _uiState.value.level == 1 && _uiState.value.score == 0
        
        if (isFirstStart || mode == "boss") {
            maxBossHealth = when(difficulty) {
                "easy" -> 150f
                "normal" -> 250f
                "hard" -> 400f
                else -> 200f
            }
            
            _uiState.value = GameUiState(
                gameMode = mode,
                difficulty = difficulty,
                targetScore = if(mode == "endless") 50 else maxBossHealth.toInt(),
                timeLeft = if (mode == "boss") 12 else 30,
                lives = if (mode == "boss") 3 else 1,
                bossHealth = 1f,
                level = if (mode == "endless" && isFirstStart) 1 else _uiState.value.level
            )
        } else {
            _uiState.value = _uiState.value.copy(
                score = 0,
                isLevelCleared = false,
                isGameOver = false,
                isPaused = false,
                activeMoles = emptyMap(),
                blockedCells = emptySet(),
                bossActionMessage = null,
                isSlowed = false
            )
        }
        
        resumeJobs()
    }

    private fun resumeJobs() {
        gameJob?.cancel()
        bossJob?.cancel()
        moleSpawnJob?.cancel()
        
        gameJob = viewModelScope.launch { startTimer() }
        moleSpawnJob = viewModelScope.launch { moleControllerLoop() }
        if (_uiState.value.gameMode == "boss") {
            bossJob = viewModelScope.launch { bossLoop() }
        }
    }

    fun pauseGame() {
        if (_uiState.value.isGameOver || _uiState.value.isLevelCleared) return
        _uiState.value = _uiState.value.copy(isPaused = true)
        gameJob?.cancel()
        bossJob?.cancel()
        moleSpawnJob?.cancel()
    }

    fun resumeGame() {
        _uiState.value = _uiState.value.copy(isPaused = false)
        resumeJobs()
    }

    private suspend fun startTimer() {
        while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver && !_uiState.value.isLevelCleared && !_uiState.value.isPaused) {
            delay(1000)
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.timeLeft - 1)
        }
        
        if (_uiState.value.timeLeft <= 0 && !_uiState.value.isLevelCleared && !_uiState.value.isPaused) {
            if (_uiState.value.gameMode == "endless") {
                _uiState.value = _uiState.value.copy(isGameOver = true)
                saveGameResult()
            } else {
                _uiState.value = _uiState.value.copy(bossActionMessage = "¡ATAQUE DEL BOSS!")
                handleLifeLoss()
            }
        }
    }

    private fun handleLifeLoss() {
        val newLives = _uiState.value.lives - 1
        if (newLives <= 0) {
            _uiState.value = _uiState.value.copy(lives = 0, isGameOver = true, activeMoles = emptyMap())
            saveGameResult()
        } else {
            _uiState.value = _uiState.value.copy(
                lives = newLives,
                timeLeft = 12, 
                activeMoles = emptyMap()
            )
            resumeJobs()
        }
    }

    private fun saveGameResult() {
        viewModelScope.launch(Dispatchers.IO) {
            val record = GameRecord(
                playerName = "Jugador 1", // En un caso real vendría de la sesión
                score = _uiState.value.score,
                mode = _uiState.value.gameMode,
                difficulty = _uiState.value.difficulty
            )
            gameDao.insertRecord(record)
        }
    }

    private fun nextLevel() {
        val nextLv = _uiState.value.level + 1
        val newTarget = 50 + (nextLv - 1) * 30
        _uiState.value = _uiState.value.copy(
            level = nextLv,
            score = 0,
            timeLeft = 30,
            targetScore = newTarget,
            isLevelCleared = false,
            activeMoles = emptyMap(),
            blockedCells = emptySet()
        )
        resumeJobs()
    }

    private suspend fun moleControllerLoop() {
        while (!_uiState.value.isGameOver && !_uiState.value.isLevelCleared && !_uiState.value.isPaused) {
            val maxMoles = if (_uiState.value.gameMode == "boss") 2 else 1
            if (_uiState.value.activeMoles.size < maxMoles) spawnMole()
            delay(300) 
        }
    }

    private fun spawnMole(forcedType: MoleType? = null) {
        val availableIndices = (0..8).filter { it !in _uiState.value.activeMoles.keys && it !in _uiState.value.blockedCells }
        if (availableIndices.isEmpty()) return
        
        val index = availableIndices.random()
        val type = forcedType ?: decideMoleType()
        
        val healthFactor = if(_uiState.value.gameMode == "boss") _uiState.value.bossHealth else 1f
        val baseLifetime = when(type) {
            MoleType.GOLDEN -> 600L
            MoleType.BOMB -> 2000L
            else -> (1000 * healthFactor).toLong().coerceAtLeast(400L)
        }

        _uiState.value = _uiState.value.copy(activeMoles = _uiState.value.activeMoles + (index to type))

        viewModelScope.launch {
            delay(if (_uiState.value.isSlowed) baseLifetime * 2 else baseLifetime)
            if (_uiState.value.activeMoles[index] == type && !_uiState.value.isPaused) {
                _uiState.value = _uiState.value.copy(activeMoles = _uiState.value.activeMoles - index)
            }
        }
    }

    private fun decideMoleType(): MoleType {
        val rand = (1..100).random()
        return if (_uiState.value.gameMode == "boss") {
            when {
                rand <= 5 -> MoleType.GOLDEN
                rand <= 25 -> MoleType.BOMB
                rand <= 40 -> MoleType.LESS_TIME
                else -> MoleType.NORMAL
            }
        } else {
            if (rand <= 15) MoleType.LESS_TIME else MoleType.NORMAL
        }
    }

    private suspend fun bossLoop() {
        while (!_uiState.value.isGameOver && !_uiState.value.isLevelCleared && !_uiState.value.isPaused) {
            delay(4000)
            if ((1..100).random() < 30) {
                val available = (0..8).filter { it !in _uiState.value.blockedCells }
                if (available.isNotEmpty()) {
                    val blocked = available.shuffled().take(1).toSet()
                    _uiState.value = _uiState.value.copy(
                        blockedCells = _uiState.value.blockedCells + blocked,
                        bossActionMessage = "¡TIERRA BLOQUEADA!"
                    )
                }
            }
        }
    }

    fun onMoleHit(index: Int) {
        val type = _uiState.value.activeMoles[index] ?: return
        if (_uiState.value.isGameOver || _uiState.value.isLevelCleared || _uiState.value.isPaused) return

        var currentScore = _uiState.value.score
        var damageToBoss = 0f

        when(type) {
            MoleType.NORMAL -> {
                damageToBoss = 10f
                _soundEvents.trySend(GameSound.HIT)
            }
            MoleType.GOLDEN -> {
                damageToBoss = 50f
                _soundEvents.trySend(GameSound.GOLDEN_HIT)
            }
            MoleType.BOMB -> {
                _soundEvents.trySend(GameSound.EXPLOSION)
                handleLifeLoss()
                return
            }
            MoleType.LESS_TIME -> {
                _uiState.value = _uiState.value.copy(timeLeft = (_uiState.value.timeLeft - 20).coerceAtLeast(0))
                _soundEvents.trySend(GameSound.HIT)
            }
            MoleType.SLOWED -> {
                _soundEvents.trySend(GameSound.HIT)
            }
        }

        if (_uiState.value.gameMode == "boss") {
            val newScore = currentScore + damageToBoss.toInt()
            val newHealth = (1f - (newScore / maxBossHealth)).coerceAtLeast(0f)
            _uiState.value = _uiState.value.copy(
                score = newScore,
                bossHealth = newHealth,
                activeMoles = _uiState.value.activeMoles - index,
                isLevelCleared = newHealth <= 0f
            )
        } else {
            val newScore = currentScore + damageToBoss.toInt()
            if (newScore >= _uiState.value.targetScore) {
                nextLevel()
            } else {
                _uiState.value = _uiState.value.copy(
                    score = newScore,
                    activeMoles = _uiState.value.activeMoles - index
                )
            }
        }
    }

    fun resetGame() {
        gameJob?.cancel()
        bossJob?.cancel()
        moleSpawnJob?.cancel()
        _uiState.value = GameUiState()
    }
}

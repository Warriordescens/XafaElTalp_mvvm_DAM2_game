package com.example.xafaeltalp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameUiState(
    val score: Int = 0,
    val timeLeft: Int = 30,
    val moleIndex: Int? = null,
    val isGameOver: Boolean = false,
    val level: Int = 1,
    val targetScore: Int = 50,
    val gameMode: String = "endless",
    val difficulty: String = "normal",
    val blockedCells: Set<Int> = emptySet(),
    val isLevelCleared: Boolean = false,
    val bossActionMessage: String? = null
)

class GameViewmodel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var bossJob: Job? = null

    fun startGame(mode: String, difficulty: String) {
        val isFirstStart = _uiState.value.level == 1 && _uiState.value.score == 0
        
        if (isFirstStart || mode == "boss") {
            val target = if (mode == "endless") 50 else 100
            _uiState.value = GameUiState(
                gameMode = mode,
                difficulty = difficulty,
                targetScore = target,
                timeLeft = if (mode == "endless") 30 else 45
            )
        } else {
            // Si es un cambio de nivel, solo reseteamos lo necesario
            _uiState.value = _uiState.value.copy(
                score = 0,
                isLevelCleared = false,
                isGameOver = false,
                moleIndex = null,
                blockedCells = emptySet(),
                bossActionMessage = null
            )
        }
        
        gameJob?.cancel()
        bossJob?.cancel()
        
        gameJob = viewModelScope.launch {
            val timer = launch { startTimer() }
            val moles = launch { moleLoop() }
            joinAll(timer, moles)
        }

        if (mode == "boss") {
            bossJob = viewModelScope.launch { bossLoop() }
        }
    }

    private suspend fun startTimer() {
        while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver && !_uiState.value.isLevelCleared) {
            delay(1000)
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.timeLeft - 1)
        }
        
        if (_uiState.value.timeLeft <= 0 && !_uiState.value.isLevelCleared) {
            // Al acabar el tiempo en endless, comprobamos si superó la puntuación
            if (_uiState.value.gameMode == "endless") {
                if (_uiState.value.score >= _uiState.value.targetScore) {
                    _uiState.value = _uiState.value.copy(isLevelCleared = true)
                    delay(1500)
                    nextLevel()
                } else {
                    _uiState.value = _uiState.value.copy(isGameOver = true, moleIndex = null)
                }
            } else {
                _uiState.value = _uiState.value.copy(isGameOver = true, moleIndex = null)
            }
        }
    }

    private fun nextLevel() {
        val nextLv = _uiState.value.level + 1
        
        // Puntuación objetivo: aumenta 30 cada nivel
        val newTarget = 50 + (nextLv - 1) * 30 
        
        _uiState.value = _uiState.value.copy(
            level = nextLv,
            timeLeft = 30,
            targetScore = newTarget
        )
        startGame(_uiState.value.gameMode, _uiState.value.difficulty)
    }

    private suspend fun moleLoop() {
        while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver && !_uiState.value.isLevelCleared) {
            var nextIndex: Int
            do {
                nextIndex = (0..8).random()
            } while (_uiState.value.blockedCells.contains(nextIndex))
            
            _uiState.value = _uiState.value.copy(moleIndex = nextIndex)
            
            // Velocidad: empezamos en 1200ms y baja 100ms por nivel hasta un mínimo de 400ms
            val delayTime = when(_uiState.value.gameMode) {
                "endless" -> (1300 - (_uiState.value.level * 100)).coerceAtLeast(400).toLong()
                else -> {
                    when(_uiState.value.difficulty) {
                        "easy" -> 1200L
                        "normal" -> 900L
                        "hard" -> 600L
                        else -> 1000L
                    }
                }
            }
            delay(delayTime)
        }
        _uiState.value = _uiState.value.copy(moleIndex = null)
    }

    private suspend fun bossLoop() {
        while (!_uiState.value.isGameOver && !_uiState.value.isLevelCleared) {
            val freq = when(_uiState.value.difficulty) {
                "easy" -> 7000L
                "normal" -> 5000L
                "hard" -> 3000L
                else -> 5000L
            }
            delay(freq)
            
            val action = (1..3).random()
            when (action) {
                1 -> { // Bloquear casillas (Tierra)
                    val numBlocked = if(_uiState.value.difficulty == "hard") 3 else 2
                    val blocked = (0..8).shuffled().take(numBlocked).toSet()
                    _uiState.value = _uiState.value.copy(blockedCells = blocked, bossActionMessage = "¡TIERRA BLOQUEADA!")
                }
                2 -> { // Bomba
                    _uiState.value = _uiState.value.copy(bossActionMessage = "¡CUIDADO: BOMBA!")
                    // Lógica visual en UI o resta puntos si el siguiente es bomba
                }
                3 -> { // Quitar tiempo
                    val timeToSub = if(_uiState.value.difficulty == "hard") 7 else 4
                    _uiState.value = _uiState.value.copy(
                        timeLeft = (_uiState.value.timeLeft - timeToSub).coerceAtLeast(0),
                        bossActionMessage = "-$timeToSub SEGUNDOS"
                    )
                }
            }
            delay(2000)
            _uiState.value = _uiState.value.copy(blockedCells = emptySet(), bossActionMessage = null)
        }
    }

    fun onMoleHit(index: Int) {
        if (_uiState.value.isGameOver || _uiState.value.isLevelCleared || _uiState.value.blockedCells.contains(index)) return

        if (_uiState.value.moleIndex == index) {
            _uiState.value = _uiState.value.copy(
                score = _uiState.value.score + 10,
                moleIndex = null
            )
            
            // En modo boss, si llegas a la puntuación, ganas inmediatamente
            if (_uiState.value.gameMode == "boss" && _uiState.value.score >= _uiState.value.targetScore) {
                _uiState.value = _uiState.value.copy(isLevelCleared = true)
            }
        }
    }

    fun resetGame() {
        gameJob?.cancel()
        bossJob?.cancel()
        _uiState.value = GameUiState()
    }
}

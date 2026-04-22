package com.example.xafaeltalp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MoleType {
    NORMAL, GOLDEN, LESS_TIME, SLOWED, BOMB
}

data class GameUiState(
    val score: Int = 0,
    val timeLeft: Int = 30,
    val activeMoles: Map<Int, MoleType> = emptyMap(),
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val level: Int = 1,
    val targetScore: Int = 50,
    val gameMode: String = "endless",
    val difficulty: String = "normal",
    val blockedCells: Set<Int> = emptySet(),
    val isLevelCleared: Boolean = false,
    val bossActionMessage: String? = null,
    val isSlowed: Boolean = false
)

class GameViewmodel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private var gameJob: Job? = null
    private var bossJob: Job? = null
    private var moleSpawnJob: Job? = null

    fun startGame(mode: String, difficulty: String) {
        val isFirstStart = _uiState.value.level == 1 && _uiState.value.score == 0
        
        if (isFirstStart || mode == "boss") {
            val (target, time) = when(mode) {
                "boss" -> when(difficulty) {
                    "easy" -> 150 to 30
                    "normal" -> 200 to 35
                    "hard" -> 250 to 45
                    else -> 150 to 30
                }
                else -> 50 to 30
            }
            
            _uiState.value = GameUiState(
                gameMode = mode,
                difficulty = difficulty,
                targetScore = target,
                timeLeft = time,
                level = if (mode == "endless") 1 else _uiState.value.level
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
        
        gameJob = viewModelScope.launch {
            startTimer()
        }

        moleSpawnJob = viewModelScope.launch {
            moleControllerLoop()
        }

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
            if (_uiState.value.gameMode == "endless" && _uiState.value.score >= _uiState.value.targetScore) {
                _uiState.value = _uiState.value.copy(isLevelCleared = true)
                delay(1500)
                nextLevel()
            } else {
                _uiState.value = _uiState.value.copy(isGameOver = true, activeMoles = emptyMap())
            }
        }
    }

    private fun nextLevel() {
        val nextLv = _uiState.value.level + 1
        val newTarget = 50 + (nextLv - 1) * 30 
        _uiState.value = _uiState.value.copy(
            level = nextLv,
            timeLeft = 30,
            targetScore = newTarget
        )
        startGame(_uiState.value.gameMode, _uiState.value.difficulty)
    }

    private suspend fun moleControllerLoop() {
        while (!_uiState.value.isGameOver && !_uiState.value.isLevelCleared && !_uiState.value.isPaused) {
            val maxMoles = if (_uiState.value.gameMode == "boss" && (_uiState.value.difficulty == "normal" || _uiState.value.difficulty == "hard")) 2 else 1
            
            if (_uiState.value.activeMoles.size < maxMoles) {
                spawnMole()
            }
            delay(300) 
        }
    }

    private fun spawnMole(forcedType: MoleType? = null) {
        val availableIndices = (0..8).filter { 
            it !in _uiState.value.activeMoles.keys && it !in _uiState.value.blockedCells 
        }
        
        if (availableIndices.isEmpty()) return
        
        val index = availableIndices.random()
        val type = forcedType ?: decideMoleType()
        
        val baseLifetime = when(type) {
            MoleType.GOLDEN -> 600L
            MoleType.BOMB -> 2000L
            MoleType.LESS_TIME -> 1500L
            MoleType.SLOWED -> 1200L
            else -> {
                if (_uiState.value.gameMode == "endless") {
                    (1300 - (_uiState.value.level * 100)).coerceAtLeast(500).toLong()
                } else {
                    when(_uiState.value.difficulty) {
                        "easy" -> 1200L
                        "normal" -> 900L
                        "hard" -> 700L
                        else -> 1000L
                    }
                }
            }
        }
        
        val lifetime = if (_uiState.value.isSlowed) baseLifetime * 2 else baseLifetime

        _uiState.value = _uiState.value.copy(
            activeMoles = _uiState.value.activeMoles + (index to type)
        )

        viewModelScope.launch {
            delay(lifetime)
            if (_uiState.value.activeMoles[index] == type && !_uiState.value.isPaused) {
                _uiState.value = _uiState.value.copy(
                    activeMoles = _uiState.value.activeMoles - index
                )
            }
        }
    }

    private fun decideMoleType(): MoleType {
        val rand = (1..100).random()
        return when {
            _uiState.value.gameMode == "boss" && rand <= 8 -> MoleType.GOLDEN
            rand <= 18 -> MoleType.LESS_TIME
            rand <= 25 -> MoleType.SLOWED
            else -> MoleType.NORMAL
        }
    }

    private suspend fun bossLoop() {
        while (!_uiState.value.isGameOver && !_uiState.value.isLevelCleared && !_uiState.value.isPaused) {
            val freq = when(_uiState.value.difficulty) {
                "easy" -> 7000L
                "normal" -> 5000L
                "hard" -> 3500L
                else -> 5000L
            }
            delay(freq)
            
            if (_uiState.value.isPaused) break

            val action = (1..3).random()
            when (action) {
                1 -> { // Bloqueo PERMANENTE
                    val available = (0..8).filter { it !in _uiState.value.blockedCells }
                    if (available.isNotEmpty()) {
                        val blocked = available.shuffled().take(1).toSet()
                        _uiState.value = _uiState.value.copy(
                            blockedCells = _uiState.value.blockedCells + blocked,
                            bossActionMessage = "¡TIERRA BLOQUEADA!"
                        )
                    }
                }
                2 -> { 
                    _uiState.value = _uiState.value.copy(bossActionMessage = "¡BOMBA!")
                    spawnMole(MoleType.BOMB)
                }
                3 -> {
                    val timeToSub = if(_uiState.value.difficulty == "hard") 8 else 5
                    _uiState.value = _uiState.value.copy(
                        timeLeft = (_uiState.value.timeLeft - timeToSub).coerceAtLeast(0),
                        bossActionMessage = "-$timeToSub SEGUNDOS"
                    )
                }
            }
            delay(2000)
            _uiState.value = _uiState.value.copy(bossActionMessage = null)
        }
    }

    fun onMoleHit(index: Int) {
        val type = _uiState.value.activeMoles[index] ?: return
        if (_uiState.value.isGameOver || _uiState.value.isLevelCleared || _uiState.value.isPaused) return

        var newScore = _uiState.value.score
        var newTimeLeft = _uiState.value.timeLeft
        var newIsSlowed = _uiState.value.isSlowed

        when(type) {
            MoleType.NORMAL -> newScore += 10
            MoleType.GOLDEN -> newScore += 100
            MoleType.LESS_TIME -> newTimeLeft = (newTimeLeft - 20).coerceAtLeast(0)
            MoleType.BOMB -> {
                newScore = (newScore - 50).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(bossActionMessage = "¡BOOM! -50")
            }
            MoleType.SLOWED -> {
                newIsSlowed = true
                viewModelScope.launch {
                    delay(5000)
                    _uiState.value = _uiState.value.copy(isSlowed = false)
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            score = newScore,
            timeLeft = newTimeLeft,
            isSlowed = newIsSlowed,
            activeMoles = _uiState.value.activeMoles - index
        )
        
        if (_uiState.value.gameMode == "boss" && _uiState.value.score >= _uiState.value.targetScore) {
            _uiState.value = _uiState.value.copy(isLevelCleared = true)
        }
    }

    fun resetGame() {
        gameJob?.cancel()
        bossJob?.cancel()
        moleSpawnJob?.cancel()
        _uiState.value = GameUiState()
    }
}

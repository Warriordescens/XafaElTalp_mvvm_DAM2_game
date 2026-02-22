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
    val isGameOver: Boolean = false
)

class GameViewmodel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private var gameJob: Job? = null

    fun startGame() {
        _uiState.value = GameUiState()
        gameJob = viewModelScope.launch {
            // Ejecutamos cronómetro y movimiento de talp en paralelo
            launch { startTimer() }
            launch { moleLoop() }
        }
    }

    private suspend fun startTimer() {
        while (_uiState.value.timeLeft > 0) {
            delay(1000)
            _uiState.value = _uiState.value.copy(timeLeft = _uiState.value.timeLeft - 1)
        }
        _uiState.value = _uiState.value.copy(isGameOver = true, moleIndex = null)
    }

    fun resetGame() {
        _uiState.value = GameUiState(
            score = 0,
            timeLeft = 30,
            moleIndex = null,
            isGameOver = false
        )
    }

    // Modificamos el loop del talp para que se detenga al acabar
    private suspend fun moleLoop() {
        while (_uiState.value.timeLeft > 0 && !_uiState.value.isGameOver) {
            _uiState.value = _uiState.value.copy(moleIndex = (0..8).random())
            delay(1500) // Un poco más rápido para nivel 1
        }
        _uiState.value = _uiState.value.copy(moleIndex = null)
    }

    fun onMoleHit(index: Int) {
        if (_uiState.value.moleIndex == index && !_uiState.value.isGameOver) {
            _uiState.value = _uiState.value.copy(
                score = _uiState.value.score + 10,
                moleIndex = null
            )
            // Forzar aparición en otro sitio rápido para feedback visual
            viewModelScope.launch {
                delay(100)
                _uiState.value = _uiState.value.copy(moleIndex = (0..8).random())
            }
        }
    }
}
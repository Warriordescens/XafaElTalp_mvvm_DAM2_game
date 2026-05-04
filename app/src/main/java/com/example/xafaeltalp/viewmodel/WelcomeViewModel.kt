package com.example.xafaeltalp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xafaeltalp.model.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow<Boolean?>(null)
    val isConnected = _isConnected.asStateFlow()

    init {
        checkApiConnection()
    }

    fun checkApiConnection() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getServiceStatus()
                // Como el JSON es una lista, cogemos el primer elemento
                _isConnected.value = response.firstOrNull()?.status == "ok"
            } catch (e: Exception) {
                _isConnected.value = false
            }
        }
    }
}

package com.example.xafaeltalp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xafaeltalp.model.AppDatabase
import com.example.xafaeltalp.model.User
import com.example.xafaeltalp.model.UserRepository
import com.example.xafaeltalp.navigation.AppScreens
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val message: String = "",
    val errorMsg: String = "",
    val isLoading: Boolean = false
)

sealed interface LoginEvent {
    data class UsernameChanged(val input: String) : LoginEvent
    data class PasswordChanged(val input: String) : LoginEvent
    data object LoginClicked : LoginEvent
    data object RegisterClicked : LoginEvent
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationChannel = Channel<String>()
    val navigationChannel = _navigationChannel.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> onUsernameChange(event.input)
            is LoginEvent.PasswordChanged -> onPasswordChange(event.input)
            is LoginEvent.LoginClicked -> onLoginClick()
            is LoginEvent.RegisterClicked -> onRegisterClick()
        }
    }

    private fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(username = input, message = "", errorMsg = "")
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }

    fun onRegisterClick() {
        val current = _uiState.value
        if (current.username.isNotBlank() && current.password.isNotBlank()) {
            viewModelScope.launch {
                val isSuccess = repository.addUser(User(current.username, current.password))
                if (isSuccess) {
                    _uiState.value = current.copy(
                        message = "¡Usuario registrado correctamente!",
                        username = "",
                        password = "",
                        errorMsg = ""
                    )
                } else {
                    _uiState.value = current.copy(errorMsg = "ERROR: ¡El usuario ya existe!", message = "")
                }
            }
        }
    }

    fun onLoginClick() {
        val current = _uiState.value
        viewModelScope.launch {
            val storedUser = repository.getUser(current.username)
            if (storedUser == null) {
                _uiState.value = current.copy(errorMsg = "ERROR: ¡El usuario no existe!", message = "")
            } else {
                if (storedUser.password == current.password) {
                    // Navegamos a la pantalla de bienvenida
                    _navigationChannel.send(AppScreens.Welcome.route)
                    _uiState.value = LoginUiState() // Limpiar campos
                } else {
                    _uiState.value = current.copy(message = "", errorMsg = "ERROR: Credenciales inválidas")
                }
            }
        }
    }
}

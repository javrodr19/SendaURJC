package com.urjc.sendaurjc.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.urjc.sendaurjc.domain.model.User
import com.urjc.sendaurjc.domain.repository.UserRepository
import com.urjc.sendaurjc.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // Alias for backward compatibility
    private val _uiState get() = _state
    val uiState get() = state

    fun checkLoggedIn() {
        viewModelScope.launch {
            if (userRepository.isLoggedIn()) {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _state.value = LoginState.Success(user)
                }
            }
        }
    }

    fun login(email: String, ssoToken: String) {
        _state.value = LoginState.Loading

        viewModelScope.launch {
            loginUseCase(email, ssoToken)
                .onSuccess { user ->
                    _state.value = LoginState.Success(user)
                }
                .onFailure { error ->
                    _state.value = LoginState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    // Keep inner sealed class as alias for any code that references LoginUiState
    sealed class LoginUiState {
        object Idle : LoginUiState()
        object Loading : LoginUiState()
        data class Success(val user: User) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }
}

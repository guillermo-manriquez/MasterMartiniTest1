package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.LoginResult
import com.example.mastermartini.data.repository.UserRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.util.validation.FieldState
import com.example.mastermartini.util.validation.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: FieldState = FieldState.initial(validate = Validators::email),
    val password: FieldState = FieldState.initial(validate = Validators::loginPassword),
    val isLoading: Boolean = false,
    val formError: String? = null,
) {
    val isValid: Boolean get() = email.isValid && password.isValid
}

/** One-time navigation signals shared by the auth screens. */
sealed interface AuthEvent {
    data object LoggedIn : AuthEvent
}

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = it.email.onValueChange(value, Validators::email), formError = null)
    }

    fun onEmailFocusLost() = _uiState.update { it.copy(email = it.email.touch()) }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = it.password.onValueChange(value, Validators::loginPassword), formError = null)
    }

    fun onPasswordFocusLost() = _uiState.update { it.copy(password = it.password.touch()) }

    fun submit() {
        val state = _uiState.updateAndGet { it.copy(email = it.email.touch(), password = it.password.touch()) }
        if (!state.isValid || state.isLoading) return

        _uiState.update { it.copy(isLoading = true, formError = null) }
        viewModelScope.launch {
            when (val result = userRepository.login(state.email.value, state.password.value)) {
                is LoginResult.Success -> {
                    sessionManager.startSession(result.user.id)
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.LoggedIn)
                }
                LoginResult.InvalidCredentials -> _uiState.update {
                    it.copy(isLoading = false, formError = INVALID_CREDENTIALS)
                }
            }
        }
    }

    companion object {
        const val INVALID_CREDENTIALS = "Correo o contraseña incorrectos"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                LoginViewModel(container.userRepository, container.sessionManager)
            }
        }
    }
}

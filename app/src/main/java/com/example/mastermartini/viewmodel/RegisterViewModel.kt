package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.RegisterResult
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

data class RegisterUiState(
    val fullName: FieldState = FieldState.initial(validate = Validators::name),
    val email: FieldState = FieldState.initial(validate = Validators::email),
    val phone: FieldState = FieldState.initial(validate = Validators::phoneCL),
    val password: FieldState = FieldState.initial(validate = Validators::password),
    val confirmPassword: FieldState = FieldState.initial { Validators.confirmPassword("", it) },
    val isLoading: Boolean = false,
) {
    val isValid: Boolean
        get() = listOf(fullName, email, phone, password, confirmPassword).all { it.isValid }
}

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = it.fullName.onValueChange(value, Validators::name))
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = it.email.onValueChange(value, Validators::email))
    }

    fun onPhoneChange(value: String) = _uiState.update {
        it.copy(phone = it.phone.onValueChange(value, Validators::phoneCL))
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(
            password = it.password.onValueChange(value, Validators::password),
            // The confirmation depends on the password, so re-check it too
            confirmPassword = it.confirmPassword.onValueChange(it.confirmPassword.value) { confirmation ->
                Validators.confirmPassword(value, confirmation)
            },
        )
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = it.confirmPassword.onValueChange(value) { confirmation ->
            Validators.confirmPassword(it.password.value, confirmation)
        })
    }

    fun onFullNameFocusLost() = _uiState.update { it.copy(fullName = it.fullName.touch()) }
    fun onEmailFocusLost() = _uiState.update { it.copy(email = it.email.touch()) }
    fun onPhoneFocusLost() = _uiState.update { it.copy(phone = it.phone.touch()) }
    fun onPasswordFocusLost() = _uiState.update { it.copy(password = it.password.touch()) }
    fun onConfirmPasswordFocusLost() = _uiState.update { it.copy(confirmPassword = it.confirmPassword.touch()) }

    fun submit() {
        val state = _uiState.updateAndGet {
            it.copy(
                fullName = it.fullName.touch(),
                email = it.email.touch(),
                phone = it.phone.touch(),
                password = it.password.touch(),
                confirmPassword = it.confirmPassword.touch(),
            )
        }
        if (!state.isValid || state.isLoading) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = userRepository.register(
                fullName = state.fullName.value,
                email = state.email.value,
                phone = state.phone.value,
                password = state.password.value,
            )
            when (result) {
                is RegisterResult.Success -> {
                    sessionManager.startSession(result.user.id)
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.LoggedIn)
                }
                RegisterResult.EmailTaken -> _uiState.update {
                    it.copy(isLoading = false, email = it.email.copy(error = EMAIL_TAKEN, touched = true))
                }
            }
        }
    }

    companion object {
        const val EMAIL_TAKEN = "Este correo ya está registrado"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                RegisterViewModel(container.userRepository, container.sessionManager)
            }
        }
    }
}

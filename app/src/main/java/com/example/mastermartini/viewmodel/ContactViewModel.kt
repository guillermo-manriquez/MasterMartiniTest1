package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.UserRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.util.validation.FieldState
import com.example.mastermartini.util.validation.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

data class ContactUiState(
    val name: FieldState = FieldState.initial(validate = Validators::name),
    val email: FieldState = FieldState.initial(validate = Validators::email),
    val phone: FieldState = FieldState.initial(validate = Validators::phoneCL),
    val message: FieldState = FieldState.initial(validate = Validators::message),
) {
    val isValid: Boolean get() = listOf(name, email, phone, message).all { it.isValid }
    val messageLength: Int get() = message.value.trim().length
}

/** Subject and body for the email app; the screen turns this into an ACTION_SENDTO intent. */
data class EmailDraft(val subject: String, val body: String)

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModel(
    userRepository: UserRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    private val _emailDrafts = Channel<EmailDraft>(Channel.BUFFERED)
    val emailDrafts: Flow<EmailDraft> = _emailDrafts.receiveAsFlow()

    init {
        // Pre-fill name, email and phone from the logged-in account (still editable and validated)
        viewModelScope.launch {
            val user = sessionManager.userId.filterNotNull()
                .flatMapLatest { userRepository.observeUser(it) }
                .filterNotNull()
                .first()
            _uiState.update {
                it.copy(
                    name = if (it.name.value.isEmpty()) FieldState.initial(user.fullName, Validators::name) else it.name,
                    email = if (it.email.value.isEmpty()) FieldState.initial(user.email, Validators::email) else it.email,
                    phone = if (it.phone.value.isEmpty()) FieldState.initial(user.phone, Validators::phoneCL) else it.phone,
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = it.name.onValueChange(value, Validators::name)) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = it.email.onValueChange(value, Validators::email)) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = it.phone.onValueChange(value, Validators::phoneCL)) }
    fun onMessageChange(value: String) =
        _uiState.update { it.copy(message = it.message.onValueChange(value, Validators::message)) }

    fun onNameFocusLost() = _uiState.update { it.copy(name = it.name.touch()) }
    fun onEmailFocusLost() = _uiState.update { it.copy(email = it.email.touch()) }
    fun onPhoneFocusLost() = _uiState.update { it.copy(phone = it.phone.touch()) }
    fun onMessageFocusLost() = _uiState.update { it.copy(message = it.message.touch()) }

    /** Validates every field; if all are valid, asks the screen to open the email app. */
    fun submit() {
        val state = _uiState.updateAndGet {
            it.copy(name = it.name.touch(), email = it.email.touch(), phone = it.phone.touch(), message = it.message.touch())
        }
        if (!state.isValid) return
        val name = state.name.value.trim()
        viewModelScope.launch {
            _emailDrafts.send(
                EmailDraft(
                    subject = "Consulta desde la app Master Martini - $name",
                    body = buildString {
                        appendLine(state.message.value.trim())
                        appendLine()
                        appendLine("--")
                        appendLine("Nombre: $name")
                        appendLine("Correo: ${state.email.value.trim()}")
                        append("Teléfono: ${state.phone.value.trim()}")
                    },
                )
            )
        }
    }

    /** The email app opened: clear the message so it is not sent twice by accident. */
    fun onEmailAppOpened() = _uiState.update { it.copy(message = FieldState.initial(validate = Validators::message)) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ContactViewModel(container.userRepository, container.sessionManager)
            }
        }
    }
}

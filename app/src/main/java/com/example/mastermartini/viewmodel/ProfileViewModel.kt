package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.ProductRepository
import com.example.mastermartini.data.repository.UserRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.model.Product
import com.example.mastermartini.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val favorites: List<Product> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    userRepository: UserRepository,
    productRepository: ProductRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = sessionManager.userId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(userRepository.observeUser(userId), productRepository.observeFavorites(userId)) { user, favorites ->
                ProfileUiState(isLoading = false, user = user, favorites = favorites)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _loggedOut = Channel<Unit>(Channel.BUFFERED)
    val loggedOut: Flow<Unit> = _loggedOut.receiveAsFlow()

    /** Clears the saved session; the account, favorites and quotes stay in the local database. */
    fun logout() {
        viewModelScope.launch {
            sessionManager.endSession()
            _loggedOut.send(Unit)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ProfileViewModel(container.userRepository, container.productRepository, container.sessionManager)
            }
        }
    }
}

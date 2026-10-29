package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AuthGateState {
    data object Checking : AuthGateState
    data object LoggedIn : AuthGateState
    data object LoggedOut : AuthGateState
}

/** Decides the first real screen from the saved session, so logged-in users never see Login. */
class AuthGateViewModel(sessionManager: SessionManager) : ViewModel() {

    val state: StateFlow<AuthGateState> = sessionManager.userId
        .map { userId -> if (userId != null) AuthGateState.LoggedIn else AuthGateState.LoggedOut }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthGateState.Checking)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthGateViewModel(appContainer().sessionManager) }
        }
    }
}

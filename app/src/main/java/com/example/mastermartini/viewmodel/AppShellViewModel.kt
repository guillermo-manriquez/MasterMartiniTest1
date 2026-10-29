package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.QuoteRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/** State for the app frame around the screens: the Cotización tab badge. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppShellViewModel(
    sessionManager: SessionManager,
    quoteRepository: QuoteRepository,
) : ViewModel() {

    val quoteItemCount: StateFlow<Int> = sessionManager.userId
        .flatMapLatest { userId -> if (userId == null) flowOf(0) else quoteRepository.observeDraftCount(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                AppShellViewModel(container.sessionManager, container.quoteRepository)
            }
        }
    }
}

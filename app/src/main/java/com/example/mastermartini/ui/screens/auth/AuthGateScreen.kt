package com.example.mastermartini.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.ui.components.MasterMartiniWordmark
import com.example.mastermartini.viewmodel.AuthGateState
import com.example.mastermartini.viewmodel.AuthGateViewModel

/** Brief branded screen shown while the saved session is read from DataStore. */
@Composable
fun AuthGateScreen(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AuthGateViewModel = viewModel(factory = AuthGateViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            AuthGateState.LoggedIn -> onLoggedIn()
            AuthGateState.LoggedOut -> onLoggedOut()
            AuthGateState.Checking -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MasterMartiniWordmark()
        CircularProgressIndicator()
    }
}

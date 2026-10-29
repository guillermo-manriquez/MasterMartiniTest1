package com.example.mastermartini.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.R
import com.example.mastermartini.ui.components.ErrorBanner
import com.example.mastermartini.ui.components.LoadingButton
import com.example.mastermartini.ui.components.MasterMartiniWordmark
import com.example.mastermartini.ui.components.ValidatedTextField
import com.example.mastermartini.viewmodel.AuthEvent
import com.example.mastermartini.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.LoggedIn -> onLoggedIn()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        MasterMartiniWordmark(showTagline = true)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.title_login), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        ErrorBanner(message = state.formError)

        ValidatedTextField(
            state = state.email,
            onValueChange = viewModel::onEmailChange,
            onFocusLost = viewModel::onEmailFocusLost,
            label = stringResource(R.string.label_email),
            leadingIcon = Icons.Outlined.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
        ValidatedTextField(
            state = state.password,
            onValueChange = viewModel::onPasswordChange,
            onFocusLost = viewModel::onPasswordFocusLost,
            label = stringResource(R.string.label_password),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                viewModel.submit()
            }),
        )

        LoadingButton(
            text = stringResource(R.string.login_button),
            loading = state.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.submit()
            },
            icon = Icons.AutoMirrored.Outlined.Login,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.login_no_account), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onGoToRegister) { Text(stringResource(R.string.login_go_register)) }
        }
    }
}

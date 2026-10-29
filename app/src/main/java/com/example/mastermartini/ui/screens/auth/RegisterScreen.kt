package com.example.mastermartini.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.R
import com.example.mastermartini.ui.components.LoadingButton
import com.example.mastermartini.ui.components.ValidatedTextField
import com.example.mastermartini.viewmodel.AuthEvent
import com.example.mastermartini.viewmodel.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.LoggedIn -> onRegistered()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.title_register)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            },
            // The app Scaffold already applies the status bar inset
            windowInsets = WindowInsets(0),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ValidatedTextField(
                state = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                onFocusLost = viewModel::onFullNameFocusLost,
                label = stringResource(R.string.label_full_name),
                leadingIcon = Icons.Outlined.Person,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = next,
            )
            ValidatedTextField(
                state = state.email,
                onValueChange = viewModel::onEmailChange,
                onFocusLost = viewModel::onEmailFocusLost,
                label = stringResource(R.string.label_email),
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            ValidatedTextField(
                state = state.phone,
                onValueChange = viewModel::onPhoneChange,
                onFocusLost = viewModel::onPhoneFocusLost,
                label = stringResource(R.string.label_phone),
                leadingIcon = Icons.Outlined.Phone,
                helperText = stringResource(R.string.helper_phone),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            ValidatedTextField(
                state = state.password,
                onValueChange = viewModel::onPasswordChange,
                onFocusLost = viewModel::onPasswordFocusLost,
                label = stringResource(R.string.label_password),
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                helperText = stringResource(R.string.helper_password),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            ValidatedTextField(
                state = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                onFocusLost = viewModel::onConfirmPasswordFocusLost,
                label = stringResource(R.string.label_confirm_password),
                leadingIcon = Icons.Outlined.LockReset,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.submit()
                }),
            )
            LoadingButton(
                text = stringResource(R.string.register_button),
                loading = state.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submit()
                },
                icon = Icons.Outlined.PersonAdd,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

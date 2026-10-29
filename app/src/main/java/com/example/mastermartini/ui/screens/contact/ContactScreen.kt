package com.example.mastermartini.ui.screens.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.R
import com.example.mastermartini.model.CompanyInfo
import com.example.mastermartini.ui.components.LoadingButton
import com.example.mastermartini.ui.components.ValidatedTextField
import com.example.mastermartini.util.openDialer
import com.example.mastermartini.util.openEmailDraft
import com.example.mastermartini.util.openMap
import com.example.mastermartini.util.openWhatsApp
import com.example.mastermartini.util.validation.Validators
import com.example.mastermartini.viewmodel.ContactViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    viewModel: ContactViewModel = viewModel(factory = ContactViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val notify: (Int) -> Unit = { res ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(context.getString(res))
        }
    }
    val launchOrExplain: (Boolean) -> Unit = { opened -> if (!opened) notify(R.string.contact_no_app) }

    LaunchedEffect(viewModel) {
        viewModel.emailDrafts.collect { draft ->
            if (context.openEmailDraft(CompanyInfo.EMAIL, draft.subject, draft.body)) {
                viewModel.onEmailAppOpened()
                notify(R.string.contact_email_opened)
            } else {
                notify(R.string.contact_no_email_app)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.contact_title)) },
                windowInsets = WindowInsets(0),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // --- Company details and direct actions ---
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.contact_company), style = MaterialTheme.typography.titleMedium)
                    InfoRow(Icons.Outlined.Phone, CompanyInfo.PHONE_DISPLAY)
                    InfoRow(Icons.AutoMirrored.Outlined.Chat, CompanyInfo.WHATSAPP_DISPLAY)
                    InfoRow(Icons.Outlined.Email, CompanyInfo.EMAIL)
                    InfoRow(Icons.Outlined.Place, CompanyInfo.ADDRESS)
                    InfoRow(Icons.Outlined.Schedule, CompanyInfo.OPENING_HOURS)
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = { launchOrExplain(context.openDialer(CompanyInfo.PHONE_DIAL)) },
                            modifier = Modifier.weight(1f),
                        ) { ActionLabel(Icons.Outlined.Call, stringResource(R.string.contact_call)) }
                        FilledTonalButton(
                            onClick = {
                                launchOrExplain(
                                    context.openWhatsApp(
                                        CompanyInfo.WHATSAPP_NUMBER,
                                        context.getString(R.string.contact_whatsapp_greeting),
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) { ActionLabel(Icons.AutoMirrored.Outlined.Chat, stringResource(R.string.contact_whatsapp)) }
                    }
                    OutlinedButton(
                        onClick = { launchOrExplain(context.openMap(CompanyInfo.ADDRESS)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { ActionLabel(Icons.Outlined.Map, stringResource(R.string.contact_map)) }
                }
            }

            // --- Contact form ---
            Text(
                stringResource(R.string.contact_form_header),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                stringResource(R.string.contact_form_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ValidatedTextField(
                state = state.name,
                onValueChange = viewModel::onNameChange,
                onFocusLost = viewModel::onNameFocusLost,
                label = stringResource(R.string.label_name),
                leadingIcon = Icons.Outlined.Person,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            ValidatedTextField(
                state = state.message,
                onValueChange = viewModel::onMessageChange,
                onFocusLost = viewModel::onMessageFocusLost,
                label = stringResource(R.string.label_message),
                leadingIcon = Icons.AutoMirrored.Outlined.Notes,
                singleLine = false,
                minLines = 4,
                helperText = stringResource(R.string.helper_message_count, state.messageLength, Validators.MESSAGE_MAX),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            LoadingButton(
                text = stringResource(R.string.contact_submit),
                loading = false,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submit()
                },
                icon = Icons.AutoMirrored.Outlined.Send,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionLabel(icon: ImageVector, text: String) {
    Icon(icon, contentDescription = null)
    Text(text, modifier = Modifier.padding(start = 8.dp))
}

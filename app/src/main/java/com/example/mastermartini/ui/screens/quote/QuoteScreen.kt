package com.example.mastermartini.ui.screens.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.R
import com.example.mastermartini.ui.components.CategoryBadge
import com.example.mastermartini.ui.components.DateField
import com.example.mastermartini.ui.components.EmptyState
import com.example.mastermartini.ui.components.ErrorBanner
import com.example.mastermartini.ui.components.LoadingButton
import com.example.mastermartini.ui.components.ValidatedTextField
import com.example.mastermartini.ui.theme.SuccessGreen
import com.example.mastermartini.viewmodel.QuoteLine
import com.example.mastermartini.viewmodel.QuoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    onGoToCatalog: () -> Unit,
    viewModel: QuoteViewModel = viewModel(factory = QuoteViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.quote_title)) },
                windowInsets = WindowInsets(0),
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // --- Draft items ---
            Text(stringResource(R.string.quote_items_header), style = MaterialTheme.typography.titleMedium)
            if (state.isEmpty) {
                EmptyState(
                    icon = Icons.Outlined.RemoveShoppingCart,
                    title = stringResource(R.string.quote_empty_title),
                    message = stringResource(R.string.quote_empty_message),
                    actionLabel = stringResource(R.string.quote_go_catalog),
                    onAction = onGoToCatalog,
                )
            } else {
                state.lines.forEach { line ->
                    QuoteLineCard(
                        line = line,
                        onQuantityChange = { viewModel.onQuantityChange(line.item.product.id, it) },
                        onIncrement = { viewModel.increment(line) },
                        onDecrement = { viewModel.decrement(line) },
                        onRemove = { viewModel.removeLine(line.item.product.id) },
                    )
                }
            }

            // --- Request form (always visible, so validation can be shown even when empty) ---
            Text(
                stringResource(R.string.quote_form_header),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
            ErrorBanner(message = state.formError)
            val form = state.form
            ValidatedTextField(
                state = form.businessName,
                onValueChange = viewModel::onBusinessNameChange,
                onFocusLost = viewModel::onBusinessNameFocusLost,
                label = stringResource(R.string.label_business_name),
                leadingIcon = Icons.Outlined.Business,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            ValidatedTextField(
                state = form.rut,
                onValueChange = viewModel::onRutChange,
                onFocusLost = viewModel::onRutFocusLost,
                label = stringResource(R.string.label_rut),
                leadingIcon = Icons.Outlined.Badge,
                helperText = stringResource(R.string.helper_rut),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = next,
            )
            DateField(
                state = form.deliveryDateField,
                selectedDate = form.deliveryDate,
                onDateSelected = viewModel::onDeliveryDateSelected,
                onFocusLost = viewModel::onDeliveryDateFocusLost,
                label = stringResource(R.string.label_delivery_date),
                isSelectable = viewModel::isSelectableDeliveryDate,
                minYear = viewModel.minDeliveryYear,
            )
            Text(
                stringResource(R.string.helper_delivery_date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp),
            )
            ValidatedTextField(
                state = form.address,
                onValueChange = { viewModel.onAddressChange(it) },
                onFocusLost = viewModel::onAddressFocusLost,
                label = stringResource(R.string.label_address),
                leadingIcon = Icons.Outlined.LocationOn,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
            LoadingButton(
                text = stringResource(R.string.quote_submit),
                loading = state.isSubmitting,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.submit()
                },
                icon = Icons.AutoMirrored.Outlined.Send,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                stringResource(R.string.quote_local_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    state.registeredRequestId?.let { requestId ->
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmation,
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen) },
            title = { Text(stringResource(R.string.quote_registered_title), textAlign = TextAlign.Center) },
            text = { Text(stringResource(R.string.quote_registered_message, requestId)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissConfirmation) { Text(stringResource(R.string.action_accept)) }
            },
        )
    }
}

@Composable
private fun QuoteLineCard(
    line: QuoteLine,
    onQuantityChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    val product = line.item.product
    val quantity = line.quantity
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryBadge(product.category, size = 40.dp)
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        product.presentation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_remove_item, product.name),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onDecrement, enabled = line.item.quantity > 1 || !quantity.isValid) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_decrease, product.name))
                }
                OutlinedTextField(
                    value = quantity.value,
                    onValueChange = onQuantityChange,
                    label = { Text(stringResource(R.string.label_quantity)) },
                    isError = quantity.showError,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier
                        .width(96.dp)
                        .semantics { if (quantity.showError) error(quantity.error.orEmpty()) },
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_increase, product.name))
                }
            }
            if (quantity.showError) {
                Text(
                    quantity.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

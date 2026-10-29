package com.example.mastermartini.ui.screens.quote

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.MyLocation
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
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
import com.example.mastermartini.ui.components.PermissionRationaleDialog
import com.example.mastermartini.ui.components.ValidatedTextField
import com.example.mastermartini.ui.theme.SuccessGreen
import com.example.mastermartini.util.permissions.PermissionResult
import com.example.mastermartini.util.permissions.openAppSettings
import com.example.mastermartini.util.permissions.rememberPermissionRequester
import com.example.mastermartini.viewmodel.LocationEvent
import com.example.mastermartini.viewmodel.QuoteLine
import com.example.mastermartini.viewmodel.QuoteViewModel
import kotlinx.coroutines.launch

private const val REMOVE_MS = 250

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    onGoToCatalog: () -> Unit,
    viewModel: QuoteViewModel = viewModel(factory = QuoteViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLocationRationale by rememberSaveable { mutableStateOf(false) }

    val showMessage: (Int, Int?, () -> Unit) -> Unit = { messageRes, actionRes, onAction ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = context.getString(messageRes),
                actionLabel = actionRes?.let(context::getString),
                duration = if (actionRes != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }

    // Location permission: granted -> fill address; declined once -> explain and offer retry;
    // permanently declined -> only the app settings can enable it
    val locationPermission = rememberPermissionRequester(
        permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    ) { result ->
        when (result) {
            PermissionResult.GRANTED -> viewModel.useCurrentLocation()
            PermissionResult.SHOW_RATIONALE -> showLocationRationale = true
            PermissionResult.PERMANENTLY_DENIED ->
                showMessage(R.string.location_permanently_denied, R.string.action_open_settings) { context.openAppSettings() }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.locationEvents.collect { event ->
            when (event) {
                LocationEvent.COORDINATES_ONLY -> showMessage(R.string.location_coordinates_only, null) {}
                LocationEvent.UNAVAILABLE -> showMessage(R.string.location_unavailable, null) {}
                LocationEvent.LOCATION_DISABLED -> showMessage(R.string.location_disabled, R.string.location_enable) {
                    context.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }
    }

    if (showLocationRationale) {
        PermissionRationaleDialog(
            icon = Icons.Outlined.MyLocation,
            title = stringResource(R.string.location_rationale_title),
            message = stringResource(R.string.location_rationale_message),
            onAllow = {
                showLocationRationale = false
                locationPermission.launchSystemDialog()
            },
            onDismiss = { showLocationRationale = false },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.quote_title)) },
                windowInsets = WindowInsets(0),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    val productId = line.item.product.id
                    key(productId) {
                        // Starts visible (no entry animation); "remove" first plays the exit
                        // animation and deletes the item from Room once it has finished
                        val visibility = remember { MutableTransitionState(true) }
                        if (visibility.isIdle && !visibility.currentState) {
                            LaunchedEffect(Unit) { viewModel.removeLine(productId) }
                        }
                        AnimatedVisibility(
                            visibleState = visibility,
                            exit = shrinkVertically(tween(REMOVE_MS)) + fadeOut(tween(REMOVE_MS)),
                        ) {
                            QuoteLineCard(
                                line = line,
                                onQuantityChange = { viewModel.onQuantityChange(productId, it) },
                                onIncrement = { viewModel.increment(line) },
                                onDecrement = { viewModel.decrement(line) },
                                onRemove = { visibility.targetState = false },
                            )
                        }
                    }
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
                extraTrailing = {
                    // Spinner replaces the button while the location is being looked up
                    if (state.isLocating) {
                        CircularProgressIndicator(Modifier.padding(12.dp).size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            locationPermission.request()
                        }) {
                            Icon(
                                Icons.Outlined.MyLocation,
                                contentDescription = stringResource(R.string.cd_use_location),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
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
            icon = { SuccessCheck() },
            title = { Text(stringResource(R.string.quote_registered_title), textAlign = TextAlign.Center) },
            text = { Text(stringResource(R.string.quote_registered_message, requestId)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissConfirmation) { Text(stringResource(R.string.action_accept)) }
            },
        )
    }
}

/** Check mark that springs in when the request is registered. */
@Composable
private fun SuccessCheck() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Icon(
        Icons.Filled.CheckCircle,
        contentDescription = null,
        tint = SuccessGreen,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
    )
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

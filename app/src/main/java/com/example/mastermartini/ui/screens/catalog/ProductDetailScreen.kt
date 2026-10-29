package com.example.mastermartini.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.R
import com.example.mastermartini.data.repository.AddToQuoteResult
import com.example.mastermartini.model.Product
import com.example.mastermartini.ui.components.AddToQuoteButton
import com.example.mastermartini.ui.components.CategoryBadge
import com.example.mastermartini.ui.components.EmptyState
import com.example.mastermartini.ui.components.FavoriteButton
import com.example.mastermartini.viewmodel.ProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onViewQuote: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // The bottom bar is hidden here, so the snackbar says where the product went
    LaunchedEffect(viewModel) {
        viewModel.addedEvents.collect { result ->
            val message = context.getString(
                if (result == AddToQuoteResult.ADDED) R.string.detail_snackbar_added else R.string.detail_snackbar_updated
            )
            snackbarHostState.currentSnackbarData?.dismiss()
            val action = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = context.getString(R.string.detail_view_quote),
                duration = SnackbarDuration.Short,
            )
            if (action == SnackbarResult.ActionPerformed) onViewQuote()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_product_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (state.product != null) {
                        FavoriteButton(isFavorite = state.isFavorite, onToggle = viewModel::toggleFavorite)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        bottomBar = {
            if (state.product != null) {
                Surface(tonalElevation = 3.dp) {
                    AddToQuoteButton(
                        feedback = state.addFeedback,
                        onClick = viewModel::addToQuote,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.notFound -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.detail_not_found_title),
                    message = stringResource(R.string.detail_not_found),
                    actionLabel = stringResource(R.string.action_back),
                    onAction = onBack,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> ProductDetailContent(product = state.product!!)
            }
        }
    }
}

@Composable
private fun ProductDetailContent(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryBadge(product.category, size = 72.dp)
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(product.category.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Text(product.description, style = MaterialTheme.typography.bodyLarge)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(stringResource(R.string.detail_family), product.family)
                HorizontalDivider()
                DetailRow(stringResource(R.string.detail_category), stringResource(product.category.labelRes))
                HorizontalDivider()
                DetailRow(stringResource(R.string.detail_presentation), product.presentation)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(
                stringResource(R.string.detail_price_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 16.dp))
    }
}

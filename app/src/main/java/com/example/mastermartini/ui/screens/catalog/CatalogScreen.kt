package com.example.mastermartini.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mastermartini.R
import com.example.mastermartini.model.CompanyInfo
import com.example.mastermartini.ui.components.CategoryChips
import com.example.mastermartini.ui.components.EmptyState
import com.example.mastermartini.ui.components.MasterMartiniWordmark
import com.example.mastermartini.ui.components.ProductCard
import com.example.mastermartini.viewmodel.CatalogViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (Long) -> Unit,
    viewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.queryText.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val openError = stringResource(R.string.catalog_open_error)

    val openWebsite: () -> Unit = {
        // No browser installed (or link blocked): explain instead of crashing
        runCatching { uriHandler.openUri(CompanyInfo.WEBSITE_URL) }
            .onFailure { scope.launch { snackbarHostState.showSnackbar(openError) } }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { MasterMartiniWordmark() },
                windowInsets = WindowInsets(0),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The app Scaffold already handles system bars and the bottom bar
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.catalog_search_label)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_search))
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            )
            CategoryChips(
                selected = state.selectedCategory,
                onSelected = viewModel::onCategorySelected,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!state.isLoading) {
                    item(key = "count") {
                        Text(
                            text = pluralStringResource(R.plurals.catalog_count, state.products.size, state.products.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                if (state.showEmptyState) {
                    item(key = "empty") {
                        EmptyState(
                            icon = Icons.Outlined.SearchOff,
                            title = stringResource(R.string.catalog_empty_title),
                            message = stringResource(R.string.catalog_empty_message),
                            actionLabel = stringResource(R.string.catalog_clear_filters).takeIf { state.hasFilters },
                            onAction = viewModel::clearFilters,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                items(state.products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        // Cards slide and fade when search or category filters change
                        modifier = Modifier.animateItem(),
                    )
                }
                // Not shown while loading: it would be the only item, and the list would keep it
                // in view (scrolled to the bottom) once the products arrive above it.
                if (!state.isLoading) item(key = "download") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .animateItem(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        OutlinedButton(onClick = openWebsite) {
                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                            Text(stringResource(R.string.catalog_download), Modifier.padding(start = 8.dp))
                        }
                        Text(
                            stringResource(R.string.catalog_download_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

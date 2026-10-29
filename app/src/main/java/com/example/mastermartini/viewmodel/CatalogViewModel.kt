package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.ProductRepository
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.model.Product
import com.example.mastermartini.model.ProductCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CatalogUiState(
    val query: String = "",
    val selectedCategory: ProductCategory? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
) {
    val hasFilters: Boolean get() = query.isNotBlank() || selectedCategory != null
    val showEmptyState: Boolean get() = !isLoading && products.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(productRepository: ProductRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ProductCategory?>(null)

    private val products = combine(query, selectedCategory) { q, c -> q to c }
        .flatMapLatest { (q, c) -> productRepository.observeProducts(q, c).map { Triple(q, c, it) } }

    val uiState: StateFlow<CatalogUiState> = products
        .map { (q, c, list) -> CatalogUiState(query = q, selectedCategory = c, products = list, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    /** The search text is kept separately so typing never waits for the database. */
    val queryText: StateFlow<String> = query

    fun onQueryChange(value: String) {
        query.value = value
    }

    /** Selecting the active category again clears it ("Todos"). */
    fun onCategorySelected(category: ProductCategory?) {
        selectedCategory.value = if (category == selectedCategory.value) null else category
    }

    fun clearFilters() {
        query.value = ""
        selectedCategory.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CatalogViewModel(appContainer().productRepository) }
        }
    }
}

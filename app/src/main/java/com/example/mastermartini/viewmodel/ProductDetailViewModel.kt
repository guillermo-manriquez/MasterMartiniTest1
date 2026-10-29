package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.mastermartini.data.repository.AddToQuoteResult
import com.example.mastermartini.data.repository.ProductRepository
import com.example.mastermartini.data.repository.QuoteRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.model.Product
import com.example.mastermartini.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val isFavorite: Boolean = false,
    /** Result of the last "Agregar" tap, shown on the button for a moment. */
    val addFeedback: AddToQuoteResult? = null,
) {
    val notFound: Boolean get() = !isLoading && product == null
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModel(
    private val productId: Long,
    private val productRepository: ProductRepository,
    private val quoteRepository: QuoteRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val addFeedback = MutableStateFlow<AddToQuoteResult?>(null)
    private var feedbackJob: Job? = null

    private val _events = Channel<AddToQuoteResult>(Channel.BUFFERED)
    /** Fires after each successful add, so the screen can offer "Ver cotización". */
    val addedEvents: Flow<AddToQuoteResult> = _events.receiveAsFlow()

    private val favoriteIds: Flow<Set<Long>> = sessionManager.userId
        .filterNotNull()
        .flatMapLatest { userId -> productRepository.observeFavoriteIds(userId) }

    val uiState: StateFlow<ProductDetailUiState> = combine(
        productRepository.observeProduct(productId),
        favoriteIds,
        addFeedback,
    ) { product, favorites, feedback ->
        ProductDetailUiState(
            isLoading = false,
            product = product,
            isFavorite = productId in favorites,
            addFeedback = feedback,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductDetailUiState())

    fun toggleFavorite() {
        viewModelScope.launch {
            val userId = currentUserId()
            productRepository.setFavorite(userId, productId, favorite = !uiState.value.isFavorite)
        }
    }

    fun addToQuote() {
        viewModelScope.launch {
            val result = quoteRepository.addToQuote(currentUserId(), productId)
            addFeedback.value = result
            _events.send(result)
            // Show "✓ Agregado" / "✓ Cantidad actualizada" for a moment, then restore the label
            feedbackJob?.cancel()
            feedbackJob = launch {
                delay(FEEDBACK_MS)
                addFeedback.value = null
            }
        }
    }

    private suspend fun currentUserId(): Long = sessionManager.userId.filterNotNull().first()

    companion object {
        const val FEEDBACK_MS = 2_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                val route = createSavedStateHandle().toRoute<Route.ProductDetail>()
                ProductDetailViewModel(
                    productId = route.productId,
                    productRepository = container.productRepository,
                    quoteRepository = container.quoteRepository,
                    sessionManager = container.sessionManager,
                )
            }
        }
    }
}

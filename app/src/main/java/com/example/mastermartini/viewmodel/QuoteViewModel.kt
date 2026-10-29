package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.QuoteRepository
import com.example.mastermartini.data.repository.SubmitQuoteResult
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.model.QuoteItem
import com.example.mastermartini.model.QuoteRequestForm
import com.example.mastermartini.model.QuoteRules
import com.example.mastermartini.util.DisplayDateFormatter
import com.example.mastermartini.util.validation.FieldState
import com.example.mastermartini.util.validation.ValidationResult
import com.example.mastermartini.util.validation.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.LocalDate

/** A draft line with the text shown in its quantity field. */
data class QuoteLine(val item: QuoteItem, val quantity: FieldState)

data class QuoteFormState(
    val businessName: FieldState = FieldState.initial(validate = Validators::businessName),
    val rut: FieldState = FieldState.initial(validate = Validators::rut),
    val deliveryDate: LocalDate? = null,
    val deliveryDateField: FieldState = FieldState.initial { Validators.deliveryDate(null, LocalDate.now()) },
    val address: FieldState = FieldState.initial(validate = Validators::address),
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    val isValid: Boolean get() = listOf(businessName, rut, deliveryDateField, address).all { it.isValid }

    fun touchAll() = copy(
        businessName = businessName.touch(),
        rut = rut.touch(),
        deliveryDateField = deliveryDateField.touch(),
        address = address.touch(),
    )
}

data class QuoteUiState(
    val isLoading: Boolean = true,
    val lines: List<QuoteLine> = emptyList(),
    val form: QuoteFormState = QuoteFormState(),
    val isSubmitting: Boolean = false,
    val formError: String? = null,
    /** Set after a successful submit, to show the confirmation dialog. */
    val registeredRequestId: Long? = null,
) {
    val isEmpty: Boolean get() = !isLoading && lines.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteViewModel(
    private val quoteRepository: QuoteRepository,
    private val sessionManager: SessionManager,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val form = MutableStateFlow(QuoteFormState())
    /** Quantity text typed by the user, per product; absent means "show the saved quantity". */
    private val quantityInputs = MutableStateFlow<Map<Long, FieldState>>(emptyMap())
    private val status = MutableStateFlow(SubmitStatus())

    private data class SubmitStatus(
        val isSubmitting: Boolean = false,
        val formError: String? = null,
        val registeredRequestId: Long? = null,
    )

    private val draft = sessionManager.userId.filterNotNull().flatMapLatest { quoteRepository.observeDraft(it) }

    val uiState: StateFlow<QuoteUiState> = combine(draft, quantityInputs, form, status) { items, inputs, form, status ->
        QuoteUiState(
            isLoading = false,
            lines = items.map { item ->
                QuoteLine(item, inputs[item.product.id] ?: FieldState(value = item.quantity.toString()))
            },
            form = form,
            isSubmitting = status.isSubmitting,
            // The "add a product" banner disappears as soon as the draft has items again
            formError = status.formError?.takeUnless { it == EMPTY_DRAFT && items.isNotEmpty() },
            registeredRequestId = status.registeredRequestId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuoteUiState())

    /** Days the date picker allows: the same rule the field is validated with. */
    fun isSelectableDeliveryDate(date: LocalDate): Boolean =
        Validators.deliveryDate(date, today()) == ValidationResult.Valid

    val minDeliveryYear: Int get() = today().year

    // --- Draft lines ---

    fun onQuantityChange(productId: Long, text: String) {
        val field = FieldState(value = text, touched = true).onValueChange(text, Validators::quantity)
        quantityInputs.update { it + (productId to field) }
        if (field.isValid) persistQuantity(productId, text.trim().toInt(), keepInput = true)
    }

    fun increment(line: QuoteLine) = step(line, +1)

    fun decrement(line: QuoteLine) = step(line, -1)

    private fun step(line: QuoteLine, delta: Int) {
        val current = line.quantity.value.trim().toIntOrNull() ?: line.item.quantity
        val next = (current + delta).coerceIn(QuoteRules.MIN_QUANTITY, QuoteRules.MAX_QUANTITY)
        persistQuantity(line.item.product.id, next, keepInput = false)
    }

    private fun persistQuantity(productId: Long, quantity: Int, keepInput: Boolean) {
        if (!keepInput) quantityInputs.update { it - productId }
        viewModelScope.launch {
            quoteRepository.updateQuantity(currentUserId(), productId, quantity)
        }
    }

    fun removeLine(productId: Long) {
        quantityInputs.update { it - productId }
        viewModelScope.launch { quoteRepository.removeItem(currentUserId(), productId) }
    }

    // --- Form fields ---

    fun onBusinessNameChange(value: String) = form.update {
        it.copy(businessName = it.businessName.onValueChange(value, Validators::businessName))
    }

    fun onRutChange(value: String) = form.update { it.copy(rut = it.rut.onValueChange(value, Validators::rut)) }

    fun onDeliveryDateSelected(date: LocalDate) = form.update {
        it.copy(
            deliveryDate = date,
            deliveryDateField = it.deliveryDateField.onValueChange(date.format(DisplayDateFormatter)) {
                Validators.deliveryDate(date, today())
            },
        )
    }

    /** Also used by T14's location autofill. */
    fun onAddressChange(value: String, latitude: Double? = null, longitude: Double? = null) = form.update {
        it.copy(address = it.address.onValueChange(value, Validators::address), latitude = latitude, longitude = longitude)
    }

    fun onBusinessNameFocusLost() = form.update { it.copy(businessName = it.businessName.touch()) }
    fun onRutFocusLost() = form.update { it.copy(rut = it.rut.touch()) }
    fun onDeliveryDateFocusLost() = form.update { it.copy(deliveryDateField = it.deliveryDateField.touch()) }
    fun onAddressFocusLost() = form.update { it.copy(address = it.address.touch()) }

    // --- Submit ---

    fun submit() {
        val current = form.updateAndGet { it.touchAll() }
        val state = uiState.value
        if (state.isSubmitting) return

        val quantitiesValid = state.lines.all { it.quantity.isValid }
        when {
            state.lines.isEmpty() -> status.update { it.copy(formError = EMPTY_DRAFT) }
            !current.isValid || !quantitiesValid -> status.update { it.copy(formError = null) }
            else -> register(current)
        }
    }

    private fun register(current: QuoteFormState) {
        status.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            val request = QuoteRequestForm(
                businessName = current.businessName.value,
                rut = current.rut.value,
                deliveryDate = checkNotNull(current.deliveryDate),
                address = current.address.value,
                latitude = current.latitude,
                longitude = current.longitude,
            )
            when (val result = quoteRepository.submitQuote(currentUserId(), request)) {
                is SubmitQuoteResult.Registered -> {
                    form.value = QuoteFormState()
                    quantityInputs.value = emptyMap()
                    status.value = SubmitStatus(registeredRequestId = result.requestId)
                }
                SubmitQuoteResult.EmptyDraft -> status.value = SubmitStatus(formError = EMPTY_DRAFT)
            }
        }
    }

    fun dismissConfirmation() = status.update { it.copy(registeredRequestId = null) }

    private suspend fun currentUserId(): Long = sessionManager.userId.filterNotNull().first()

    companion object {
        const val EMPTY_DRAFT = "Agrega al menos un producto"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                QuoteViewModel(container.quoteRepository, container.sessionManager)
            }
        }
    }
}

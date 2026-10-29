package com.example.mastermartini.data.repository

import com.example.mastermartini.data.local.dao.QuoteDao
import com.example.mastermartini.data.local.entity.QuoteRequestEntity
import com.example.mastermartini.model.QuoteItem
import com.example.mastermartini.model.QuoteRequestForm
import com.example.mastermartini.model.QuoteRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AddToQuoteResult { ADDED, QUANTITY_UPDATED }

sealed interface SubmitQuoteResult {
    data class Registered(val requestId: Long) : SubmitQuoteResult
    data object EmptyDraft : SubmitQuoteResult
}

interface QuoteRepository {
    fun observeDraft(userId: Long): Flow<List<QuoteItem>>

    /** Number of distinct products in the draft, for the tab badge. */
    fun observeDraftCount(userId: Long): Flow<Int>

    /** Adds the product with quantity 1, or adds 1 if it is already in the draft (max 999). */
    suspend fun addToQuote(userId: Long, productId: Long): AddToQuoteResult

    /** Sets an already-validated quantity (1–999). */
    suspend fun updateQuantity(userId: Long, productId: Long, quantity: Int)

    suspend fun removeItem(userId: Long, productId: Long)

    /** Saves the request with a copy of the draft and clears the draft. Nothing is sent anywhere. */
    suspend fun submitQuote(userId: Long, form: QuoteRequestForm): SubmitQuoteResult
}

class RoomQuoteRepository(
    private val quoteDao: QuoteDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : QuoteRepository {

    override fun observeDraft(userId: Long): Flow<List<QuoteItem>> =
        quoteDao.observeDraft(userId).map { list -> list.map { it.toModel() } }

    override fun observeDraftCount(userId: Long): Flow<Int> = quoteDao.observeDraftCount(userId)

    override suspend fun addToQuote(userId: Long, productId: Long): AddToQuoteResult {
        val added = quoteDao.addOrIncrement(userId, productId, QuoteRules.MAX_QUANTITY)
        return if (added) AddToQuoteResult.ADDED else AddToQuoteResult.QUANTITY_UPDATED
    }

    override suspend fun updateQuantity(userId: Long, productId: Long, quantity: Int) {
        require(quantity in QuoteRules.MIN_QUANTITY..QuoteRules.MAX_QUANTITY) { "Invalid quantity: $quantity" }
        quoteDao.updateQuantity(userId, productId, quantity)
    }

    override suspend fun removeItem(userId: Long, productId: Long) = quoteDao.deleteItem(userId, productId)

    override suspend fun submitQuote(userId: Long, form: QuoteRequestForm): SubmitQuoteResult {
        val request = QuoteRequestEntity(
            userId = userId,
            businessName = form.businessName.trim(),
            rut = form.rut.trim(),
            deliveryDate = form.deliveryDate,
            address = form.address.trim(),
            latitude = form.latitude,
            longitude = form.longitude,
            createdAt = clock(),
        )
        return quoteDao.submitDraft(request)
            ?.let { SubmitQuoteResult.Registered(it) }
            ?: SubmitQuoteResult.EmptyDraft
    }
}

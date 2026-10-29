package com.example.mastermartini.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.mastermartini.data.local.entity.QuoteItemEntity
import com.example.mastermartini.data.local.entity.QuoteItemWithProduct
import com.example.mastermartini.data.local.entity.QuoteRequestEntity
import com.example.mastermartini.data.local.entity.QuoteRequestItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuoteDao {

    // --- Draft quote ---

    @Query(
        """
        SELECT p.*, q.quantity FROM quote_items q
        INNER JOIN products p ON p.id = q.productId
        WHERE q.userId = :userId
        ORDER BY p.name
        """
    )
    abstract fun observeDraft(userId: Long): Flow<List<QuoteItemWithProduct>>

    /** Number of distinct products in the draft (shown as the tab badge). */
    @Query("SELECT COUNT(*) FROM quote_items WHERE userId = :userId")
    abstract fun observeDraftCount(userId: Long): Flow<Int>

    @Query("SELECT quantity FROM quote_items WHERE userId = :userId AND productId = :productId")
    protected abstract suspend fun findQuantity(userId: Long, productId: Long): Int?

    @Insert
    protected abstract suspend fun insertItem(item: QuoteItemEntity)

    @Query("UPDATE quote_items SET quantity = :quantity WHERE userId = :userId AND productId = :productId")
    abstract suspend fun updateQuantity(userId: Long, productId: Long, quantity: Int)

    @Query("DELETE FROM quote_items WHERE userId = :userId AND productId = :productId")
    abstract suspend fun deleteItem(userId: Long, productId: Long)

    @Query("SELECT * FROM quote_items WHERE userId = :userId")
    protected abstract suspend fun getDraftItems(userId: Long): List<QuoteItemEntity>

    @Query("DELETE FROM quote_items WHERE userId = :userId")
    protected abstract suspend fun clearDraft(userId: Long)

    /**
     * "Agregar a cotización": inserts the product with quantity 1, or adds 1 if it is
     * already in the draft, without going over [maxQuantity].
     * Returns true if the product was new to the draft.
     */
    @Transaction
    open suspend fun addOrIncrement(userId: Long, productId: Long, maxQuantity: Int): Boolean {
        val current = findQuantity(userId, productId)
        return if (current == null) {
            insertItem(QuoteItemEntity(userId, productId, quantity = 1))
            true
        } else {
            updateQuantity(userId, productId, minOf(current + 1, maxQuantity))
            false
        }
    }

    // --- Submitted requests ---

    @Insert
    protected abstract suspend fun insertRequest(request: QuoteRequestEntity): Long

    @Insert
    protected abstract suspend fun insertRequestItems(items: List<QuoteRequestItemEntity>)

    @Query("SELECT id, name FROM products WHERE id IN (:ids)")
    protected abstract suspend fun getProductNames(ids: List<Long>): List<ProductName>

    /**
     * Saves [request] with a copy of the user's draft items and clears the draft,
     * all in one transaction. Returns the new request id, or null if the draft is empty.
     */
    @Transaction
    open suspend fun submitDraft(request: QuoteRequestEntity): Long? {
        val draft = getDraftItems(request.userId)
        if (draft.isEmpty()) return null
        val names = getProductNames(draft.map { it.productId }).associate { it.id to it.name }
        val requestId = insertRequest(request)
        insertRequestItems(
            draft.map {
                QuoteRequestItemEntity(
                    requestId = requestId,
                    productId = it.productId,
                    productName = names.getValue(it.productId),
                    quantity = it.quantity,
                )
            }
        )
        clearDraft(request.userId)
        return requestId
    }

    protected data class ProductName(val id: Long, val name: String)
}

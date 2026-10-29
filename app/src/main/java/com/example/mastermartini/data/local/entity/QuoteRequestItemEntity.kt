package com.example.mastermartini.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Copy of a draft item taken when the quote is submitted. The product name is
 * copied too, so the request keeps its contents even if the catalog changes.
 */
@Entity(
    tableName = "quote_request_items",
    primaryKeys = ["requestId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = QuoteRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class QuoteRequestItemEntity(
    val requestId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
)

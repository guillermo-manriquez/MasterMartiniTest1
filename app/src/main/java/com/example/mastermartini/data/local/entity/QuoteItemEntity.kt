package com.example.mastermartini.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** One product in a user's draft quote (the "Cotización" tab before submitting). */
@Entity(
    tableName = "quote_items",
    primaryKeys = ["userId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("productId")],
)
data class QuoteItemEntity(
    val userId: Long,
    val productId: Long,
    val quantity: Int,
)

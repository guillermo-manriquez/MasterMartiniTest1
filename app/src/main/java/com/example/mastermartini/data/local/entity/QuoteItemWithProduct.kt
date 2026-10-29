package com.example.mastermartini.data.local.entity

import androidx.room.Embedded

/** Query result for the draft quote list: the product plus its quantity. */
data class QuoteItemWithProduct(
    @Embedded val product: ProductEntity,
    val quantity: Int,
)

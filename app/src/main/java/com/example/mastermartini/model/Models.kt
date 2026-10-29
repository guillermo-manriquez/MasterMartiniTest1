package com.example.mastermartini.model

import java.time.LocalDate

/*
 * Domain models used by ViewModels and screens. Repositories map Room entities
 * to these, so the UI never sees password hashes or database-only fields.
 */

data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val photoUri: String?,
)

data class Product(
    val id: Long,
    val name: String,
    val family: String,
    val category: ProductCategory,
    val description: String,
    val presentation: String,
)

data class QuoteItem(
    val product: Product,
    val quantity: Int,
)

/** Data entered in the quote form, already validated by the ViewModel. */
data class QuoteRequestForm(
    val businessName: String,
    val rut: String,
    val deliveryDate: LocalDate,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

object QuoteRules {
    const val MIN_QUANTITY = 1
    const val MAX_QUANTITY = 999
}

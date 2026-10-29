package com.example.mastermartini.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations (Navigation Compose 2.8+). */
sealed interface Route {

    /** Start destination: reads the saved session and redirects to Catalog or Login. */
    @Serializable data object AuthGate : Route

    @Serializable data object Login : Route

    @Serializable data object Register : Route

    // Bottom bar tabs
    @Serializable data object Catalog : Route

    @Serializable data object Quote : Route

    @Serializable data object Contact : Route

    @Serializable data object Profile : Route

    @Serializable data class ProductDetail(val productId: Long) : Route
}

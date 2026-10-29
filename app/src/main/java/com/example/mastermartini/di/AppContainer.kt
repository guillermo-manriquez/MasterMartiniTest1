package com.example.mastermartini.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mastermartini.MasterMartiniApp
import com.example.mastermartini.data.local.AppDatabase
import com.example.mastermartini.data.repository.ProductRepository
import com.example.mastermartini.data.repository.QuoteRepository
import com.example.mastermartini.data.repository.RoomProductRepository
import com.example.mastermartini.data.repository.RoomQuoteRepository
import com.example.mastermartini.data.repository.RoomUserRepository
import com.example.mastermartini.data.repository.UserRepository
import com.example.mastermartini.data.session.DataStoreSessionManager
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.util.security.PasswordHasher
import com.example.mastermartini.util.security.Pbkdf2PasswordHasher

/**
 * Manual dependency injection: the single place where app-wide objects are built.
 * ViewModels get their dependencies from here through [appContainer] inside
 * `viewModelFactory { initializer { ... } }`, so screens never construct data classes.
 */
interface AppContainer {
    val database: AppDatabase
    val sessionManager: SessionManager
    val userRepository: UserRepository
    val productRepository: ProductRepository
    val quoteRepository: QuoteRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val passwordHasher: PasswordHasher by lazy { Pbkdf2PasswordHasher() }

    override val database: AppDatabase by lazy { AppDatabase.build(context, passwordHasher) }

    override val sessionManager: SessionManager by lazy { DataStoreSessionManager(context) }

    override val userRepository: UserRepository by lazy {
        RoomUserRepository(database.userDao(), passwordHasher)
    }

    override val productRepository: ProductRepository by lazy {
        RoomProductRepository(database.productDao(), database.favoriteDao())
    }

    override val quoteRepository: QuoteRepository by lazy { RoomQuoteRepository(database.quoteDao()) }
}

/** Resolves the [AppContainer] from a ViewModel factory's [CreationExtras]. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as MasterMartiniApp).container

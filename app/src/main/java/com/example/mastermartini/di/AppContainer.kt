package com.example.mastermartini.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mastermartini.MasterMartiniApp
import com.example.mastermartini.data.local.AppDatabase

/**
 * Manual dependency injection: the single place where app-wide objects are built.
 * ViewModels get their dependencies from here through [appContainer] inside
 * `viewModelFactory { initializer { ... } }`, so screens never construct data classes.
 *
 * T5 adds the repositories and the DataStore session.
 */
interface AppContainer {
    val database: AppDatabase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy { AppDatabase.build(context) }
}

/** Resolves the [AppContainer] from a ViewModel factory's [CreationExtras]. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as MasterMartiniApp).container

package com.example.mastermartini.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mastermartini.MasterMartiniApp

/**
 * Manual dependency injection: the single place where app-wide objects are built.
 * ViewModels get their dependencies from here through [appContainer] inside
 * `viewModelFactory { initializer { ... } }`, so screens never construct data classes.
 *
 * T4 adds the Room database; T5 adds the repositories and the DataStore session.
 */
interface AppContainer

class DefaultAppContainer(
    @Suppress("unused") private val context: Context,
) : AppContainer

/** Resolves the [AppContainer] from a ViewModel factory's [CreationExtras]. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as MasterMartiniApp).container

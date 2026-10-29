package com.example.mastermartini.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Keeps track of the logged-in user across app restarts. */
interface SessionManager {
    /** Id of the logged-in user, or null when nobody is logged in. */
    val userId: Flow<Long?>

    suspend fun startSession(userId: Long)

    suspend fun endSession()
}

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class DataStoreSessionManager(context: Context) : SessionManager {

    private val dataStore = context.sessionDataStore

    override val userId: Flow<Long?> = dataStore.data
        .map { preferences -> preferences[USER_ID] }
        .distinctUntilChanged()

    override suspend fun startSession(userId: Long) {
        dataStore.edit { preferences -> preferences[USER_ID] = userId }
    }

    override suspend fun endSession() {
        dataStore.edit { preferences -> preferences.remove(USER_ID) }
    }

    private companion object {
        val USER_ID = longPreferencesKey("logged_in_user_id")
    }
}

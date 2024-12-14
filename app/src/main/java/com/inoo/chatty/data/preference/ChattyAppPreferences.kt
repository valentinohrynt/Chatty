package com.inoo.chatty.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ChattyAppPreferences")

class ChattyAppPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val HAS_LOGGED_IN = booleanPreferencesKey("has_logged_in")
    }

    val hasLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_LOGGED_IN] == true
        }

    suspend fun setLoggedIn() {
        context.dataStore.edit { preferences ->
            preferences[HAS_LOGGED_IN] = true
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
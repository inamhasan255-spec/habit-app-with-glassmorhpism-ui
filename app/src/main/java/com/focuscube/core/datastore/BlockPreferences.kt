package com.focuscube.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_cube_block_prefs")

class BlockPreferences(private val context: Context) {

    companion object {
        private val IS_SESSION_LOCKED = booleanPreferencesKey("is_session_locked")
        private val RESTRICTED_PACKAGES = stringSetPreferencesKey("restricted_packages")
        
        private val DEFAULT_RESTRICTED = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.google.android.youtube",
            "com.facebook.katana"
        )
    }

    val isSessionLocked: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_SESSION_LOCKED] ?: false
        }

    val restrictedPackages: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[RESTRICTED_PACKAGES] ?: DEFAULT_RESTRICTED
        }

    suspend fun setSessionLock(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SESSION_LOCKED] = locked
        }
    }

    suspend fun addRestrictedPackage(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[RESTRICTED_PACKAGES] ?: DEFAULT_RESTRICTED
            preferences[RESTRICTED_PACKAGES] = current + packageName
        }
    }

    suspend fun removeRestrictedPackage(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[RESTRICTED_PACKAGES] ?: DEFAULT_RESTRICTED
            preferences[RESTRICTED_PACKAGES] = current - packageName
        }
    }

    suspend fun setRestrictedPackages(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[RESTRICTED_PACKAGES] = packages
        }
    }
}

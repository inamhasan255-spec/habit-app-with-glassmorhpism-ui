package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "focus_cube_datastore")

class DataStoreManager(private val context: Context) {
    companion object {
        private val ESSENTIAL_APPS_KEY = stringPreferencesKey("essential_apps_whitelist")
        val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        
        val GALAXY_VISIBLE_KEY = booleanPreferencesKey("galaxy_visible")
        val GALAXY_STAR_SPEED_KEY = androidx.datastore.preferences.core.floatPreferencesKey("galaxy_star_speed")
        val GALAXY_HUE_SHIFT_KEY = androidx.datastore.preferences.core.floatPreferencesKey("galaxy_hue_shift")
    }

    val whitelistedApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[ESSENTIAL_APPS_KEY] ?: "com.android.phone,com.google.android.dialer,com.google.android.apps.messaging,com.android.providers.telephony,com.google.android.apps.maps,com.android.chrome"
        raw.split(",").filter { it.isNotEmpty() }.toSet()
    }

    suspend fun saveWhitelistedApps(apps: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[ESSENTIAL_APPS_KEY] = apps.joinToString(",")
        }
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETE_KEY] ?: false
    }

    val userRole: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY] ?: "STUDENT"
    }

    val galaxyVisible: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GALAXY_VISIBLE_KEY] ?: true
    }

    val galaxyStarSpeed: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GALAXY_STAR_SPEED_KEY] ?: 1.0f
    }

    val galaxyHueShift: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GALAXY_HUE_SHIFT_KEY] ?: 0.0f
    }

    suspend fun saveGalaxyPreferences(visible: Boolean, starSpeed: Float, hueShift: Float) {
        context.dataStore.edit { preferences ->
            preferences[GALAXY_VISIBLE_KEY] = visible
            preferences[GALAXY_STAR_SPEED_KEY] = starSpeed
            preferences[GALAXY_HUE_SHIFT_KEY] = hueShift
        }
    }

    suspend fun completeOnboarding(role: String) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETE_KEY] = true
            preferences[USER_ROLE_KEY] = role
        }
    }
}

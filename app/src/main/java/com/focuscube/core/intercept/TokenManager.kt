package com.focuscube.core.intercept

import android.content.Context
import android.content.SharedPreferences

class TokenManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dopamine_token_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val TOKEN_LIFETIME_MS = 120_000L // 120 seconds
        
        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun grantToken() {
        val expiryTime = System.currentTimeMillis() + TOKEN_LIFETIME_MS
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply()
    }

    fun hasValidToken(): Boolean {
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        return System.currentTimeMillis() < expiryTime
    }

    fun revokeToken() {
        prefs.edit().remove(KEY_TOKEN_EXPIRY).apply()
    }
    
    fun getRemainingTimeMs(): Long {
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        val remaining = expiryTime - System.currentTimeMillis()
        return kotlin.math.max(0L, remaining)
    }
}

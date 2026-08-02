package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ShieldRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
    
    private val _isShieldActive = MutableStateFlow(prefs.getBoolean("is_shield_active_for_service", true))
    val isShieldActive: StateFlow<Boolean> = _isShieldActive

    fun setShieldActive(active: Boolean) {
        prefs.edit().putBoolean("is_shield_active_for_service", active).apply()
        _isShieldActive.value = active
    }
}

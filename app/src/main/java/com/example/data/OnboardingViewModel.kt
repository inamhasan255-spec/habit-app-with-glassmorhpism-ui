package com.example.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    
    val onboardingComplete: StateFlow<Boolean> = dataStoreManager.onboardingComplete.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val userRole: StateFlow<String> = dataStoreManager.userRole.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "STUDENT"
    )

    fun completeOnboarding(role: String, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            dataStoreManager.completeOnboarding(role)
            onFinished()
        }
    }
}

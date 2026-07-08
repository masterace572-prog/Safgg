package com.ludovault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludovault.data.model.Settings
import com.ludovault.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 */
class SettingsViewModel(private val repository: GameRepository) : ViewModel() {

    val settings: StateFlow<Settings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun updateUpiId(upiId: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(upiId = upiId))
        }
    }

    fun updateTheme(mode: Int) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(themeMode = mode))
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(soundEnabled = enabled))
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }
}

package com.ludovault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludovault.data.model.Statistics
import com.ludovault.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the home screen.
 */
class HomeViewModel(private val repository: GameRepository) : ViewModel() {

    val statistics: StateFlow<Statistics> = repository.statistics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Statistics()
        )

    val settings: StateFlow<com.ludovault.data.model.Settings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.ludovault.data.model.Settings()
        )

    /**
     * Adds coins to the user's balance.
     */
    fun rechargeCoins(amount: Int) {
        viewModelScope.launch {
            val current = repository.statistics.first()
            val newCoins = current.currentCoins + amount
            val newHighest = maxOf(current.highestCoins, newCoins)
            repository.saveStatistics(
                current.copy(
                    currentCoins = newCoins,
                    highestCoins = newHighest
                )
            )
        }
    }
}

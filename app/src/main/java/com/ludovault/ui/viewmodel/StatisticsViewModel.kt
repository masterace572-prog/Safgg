package com.ludovault.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ludovault.data.model.Statistics
import com.ludovault.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the statistics screen.
 */
class StatisticsViewModel(repository: GameRepository) : ViewModel() {

    val statistics: StateFlow<Statistics> = repository.statistics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Statistics()
        )
}

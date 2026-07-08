package com.ludovault.data.repository

import com.ludovault.data.DataStoreManager
import com.ludovault.data.model.Settings
import com.ludovault.data.model.Statistics
import kotlinx.coroutines.flow.Flow

/**
 * Repository that abstracts data operations for statistics and settings.
 *
 * @param dataStoreManager The DataStore manager instance.
 */
class GameRepository(private val dataStoreManager: DataStoreManager) {

    val statistics: Flow<Statistics> = dataStoreManager.statisticsFlow
    val settings: Flow<Settings> = dataStoreManager.settingsFlow

    /**
     * Saves updated statistics.
     */
    suspend fun saveStatistics(stats: Statistics) {
        dataStoreManager.updateStatistics(stats)
    }

    /**
     * Saves updated settings.
     */
    suspend fun saveSettings(settings: Settings) {
        dataStoreManager.updateSettings(settings)
    }

    /**
     * Resets all progress.
     */
    suspend fun resetAllProgress() {
        dataStoreManager.resetProgress()
    }
}

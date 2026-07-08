package com.ludovault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ludovault.data.model.Settings
import com.ludovault.data.model.Statistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages local persistence using Jetpack DataStore.
 */
class DataStoreManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ludo_vault_prefs")

    companion object {
        private val CURRENT_COINS = intPreferencesKey("current_coins")
        private val HIGHEST_COINS = intPreferencesKey("highest_coins")
        private val WINS = intPreferencesKey("wins")
        private val LOSSES = intPreferencesKey("losses")
        private val MATCHES_PLAYED = intPreferencesKey("matches_played")
        private val UPI_ID = stringPreferencesKey("upi_id")
        private val THEME_MODE = intPreferencesKey("theme_mode")
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    /**
     * Flow of user statistics.
     */
    val statisticsFlow: Flow<Statistics> = context.dataStore.data.map { prefs ->
        Statistics(
            currentCoins = prefs[CURRENT_COINS] ?: Statistics.INITIAL_COINS,
            highestCoins = prefs[HIGHEST_COINS] ?: Statistics.INITIAL_COINS,
            wins = prefs[WINS] ?: 0,
            losses = prefs[LOSSES] ?: 0,
            matchesPlayed = prefs[MATCHES_PLAYED] ?: 0
        )
    }

    /**
     * Flow of app settings.
     */
    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            upiId = prefs[UPI_ID] ?: "",
            themeMode = prefs[THEME_MODE] ?: 0,
            soundEnabled = prefs[SOUND_ENABLED] != false
        )
    }

    /**
     * Updates statistics in DataStore.
     */
    suspend fun updateStatistics(stats: Statistics) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_COINS] = stats.currentCoins
            prefs[HIGHEST_COINS] = stats.highestCoins
            prefs[WINS] = stats.wins
            prefs[LOSSES] = stats.losses
            prefs[MATCHES_PLAYED] = stats.matchesPlayed
        }
    }

    /**
     * Updates settings in DataStore.
     */
    suspend fun updateSettings(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[UPI_ID] = settings.upiId
            prefs[THEME_MODE] = settings.themeMode
            prefs[SOUND_ENABLED] = settings.soundEnabled
        }
    }

    /**
     * Resets all game progress to initial values.
     */
    suspend fun resetProgress() {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_COINS] = Statistics.INITIAL_COINS
            prefs[HIGHEST_COINS] = Statistics.INITIAL_COINS
            prefs[WINS] = 0
            prefs[LOSSES] = 0
            prefs[MATCHES_PLAYED] = 0
        }
    }
}

package com.ludovault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.ludovault.data.DataStoreManager
import com.ludovault.ui.navigation.LudoVaultNavGraph
import com.ludovault.ui.theme.LudoVaultTheme
import kotlinx.coroutines.launch

/**
 * Main entry point for Ludo Vault.
 */
class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dataStoreManager = DataStoreManager(this)

        // Ensure initial stats exist
        lifecycleScope.launch {
            val stats = dataStoreManager.statisticsFlow.first()
            if (stats.matchesPlayed == 0 && stats.currentCoins == 0) {
                dataStoreManager.updateStatistics(
                    com.ludovault.data.model.Statistics()
                )
            }
        }

        setContent {
            val settings by dataStoreManager.settingsFlow
                .collectAsStateWithLifecycle(initialValue = com.ludovault.data.model.Settings())

            LudoVaultTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    LudoVaultNavGraph(
                        navController = navController,
                        dataStoreManager = dataStoreManager
                    )
                }
            }
        }
    }
}

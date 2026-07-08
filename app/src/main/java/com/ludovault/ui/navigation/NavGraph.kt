package com.ludovault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ludovault.data.DataStoreManager
import com.ludovault.data.repository.GameRepository
import com.ludovault.ui.screens.GameScreen
import com.ludovault.ui.screens.HomeScreen
import com.ludovault.ui.screens.OutOfCoinsScreen
import com.ludovault.ui.screens.RechargeScreen
import com.ludovault.ui.screens.SettingsScreen
import com.ludovault.ui.screens.StakeScreen
import com.ludovault.ui.screens.StatisticsScreen
import com.ludovault.ui.viewmodel.GameViewModel
import com.ludovault.ui.viewmodel.HomeViewModel
import com.ludovault.ui.viewmodel.SettingsViewModel
import com.ludovault.ui.viewmodel.StatisticsViewModel

/**
 * Navigation routes for Ludo Vault.
 */
object Routes {
    const val HOME = "home"
    const val STAKE = "stake"
    const val GAME = "game"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val RECHARGE = "recharge"
    const val OUT_OF_COINS = "out_of_coins"
}

/**
 * Sets up the navigation graph for the app.
 */
@Composable
fun LudoVaultNavGraph(
    navController: NavHostController,
    dataStoreManager: DataStoreManager
) {
    val repository = GameRepository(dataStoreManager)

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            HomeScreen(
                viewModel = viewModel,
                onPlayClick = { navController.navigate(Routes.STAKE) },
                onStatisticsClick = { navController.navigate(Routes.STATISTICS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.STAKE) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            StakeScreen(
                viewModel = viewModel,
                onStakeSelected = { stake ->
                    navController.navigate("${Routes.GAME}?stake=$stake") {
                        popUpTo(Routes.STAKE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("${Routes.GAME}?stake={stake}") { backStackEntry ->
            val stake = backStackEntry.arguments?.getString("stake")?.toIntOrNull() ?: 50
            val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))
            GameScreen(
                viewModel = viewModel,
                stake = stake,
                onExit = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onOutOfCoins = {
                    navController.navigate(Routes.OUT_OF_COINS) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
        composable(Routes.STATISTICS) {
            val viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModelFactory(repository))
            StatisticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository))
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.RECHARGE) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            RechargeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRecharged = { navController.popBackStack() }
            )
        }
        composable(Routes.OUT_OF_COINS) {
            OutOfCoinsScreen(
                onRecharge = { navController.navigate(Routes.RECHARGE) },
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

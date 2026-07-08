package com.ludovault.data.model

/**
 * User statistics persisted across sessions.
 *
 * @param currentCoins Current virtual coin balance.
 * @param highestCoins Highest coin balance ever achieved.
 * @param wins Total matches won.
 * @param losses Total matches lost.
 * @param matchesPlayed Total matches played.
 */
data class Statistics(
    val currentCoins: Int = 10000,
    val highestCoins: Int = 10000,
    val wins: Int = 0,
    val losses: Int = 0,
    val matchesPlayed: Int = 0
) {
    companion object {
        const val INITIAL_COINS = 10000
        const val MIN_PLAY_COINS = 50
    }
}

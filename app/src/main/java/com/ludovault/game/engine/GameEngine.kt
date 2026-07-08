package com.ludovault.game.engine

import com.ludovault.data.model.GamePhase
import com.ludovault.data.model.GameState
import com.ludovault.data.model.Player
import com.ludovault.data.model.PlayerColor
import com.ludovault.data.model.PlayerType
import com.ludovault.data.model.Statistics

/**
 * Core game engine that manages Ludo match lifecycle.
 */
class GameEngine {

    /**
     * Creates a new game with one human and three bots.
     *
     * @param stake The coin stake for this match.
     */
    fun createGame(stake: Int): GameState {
        val players = listOf(
            Player(color = PlayerColor.RED, type = PlayerType.HUMAN),
            Player(color = PlayerColor.GREEN, type = PlayerType.BOT),
            Player(color = PlayerColor.YELLOW, type = PlayerType.BOT),
            Player(color = PlayerColor.BLUE, type = PlayerType.BOT)
        )
        return GameState(
            players = players,
            currentPlayerIndex = 0,
            phase = GamePhase.ROLLING,
            stake = stake
        )
    }

    /**
     * Rolls the dice for the current player.
     *
     * @return Pair of (new state, dice value).
     */
    fun rollDice(state: GameState): Pair<GameState, Int> {
        if (!MoveValidator.canRoll(state)) return state to state.diceValue

        val dice = (1..6).random()
        val newState = state.copy(
            diceValue = dice,
            diceRolled = true,
            phase = GamePhase.SELECTING_TOKEN,
            consecutiveSixes = if (dice == 6) state.consecutiveSixes else 0
        )

        // If no moves available, pass turn
        if (!MoveValidator.hasAnyMove(newState)) {
            val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
            return newState.copy(
                currentPlayerIndex = nextIndex,
                diceValue = 0,
                diceRolled = false,
                phase = GamePhase.ROLLING,
                consecutiveSixes = 0
            ) to dice
        }

        return newState to dice
    }

    /**
     * Selects a token to move.
     */
    fun selectToken(state: GameState, tokenId: Int): GameState {
        if (state.phase != GamePhase.SELECTING_TOKEN) return state
        if (!MoveValidator.canMoveToken(state, tokenId)) return state
        return state.copy(selectedTokenId = tokenId, phase = GamePhase.MOVING)
    }

    /**
     * Confirms movement of the selected token.
     */
    fun confirmMove(state: GameState): GameState {
        if (state.phase != GamePhase.MOVING || state.selectedTokenId == -1) return state
        return MoveValidator.executeMove(state, state.selectedTokenId)
    }

    /**
     * Calculates statistics after a match ends.
     *
     * @param state Final game state.
     * @param currentStats Current user statistics.
     * @return Updated statistics.
     */
    fun calculateMatchResult(state: GameState, currentStats: Statistics): Statistics {
        val isWin = state.winnerColor == PlayerColor.RED
        val newMatches = currentStats.matchesPlayed + 1
        val newWins = currentStats.wins + if (isWin) 1 else 0
        val newLosses = currentStats.losses + if (!isWin) 1 else 0
        val coinChange = if (isWin) state.stake * 2 else 0
        val newCoins = currentStats.currentCoins - state.stake + coinChange
        val newHighest = maxOf(currentStats.highestCoins, newCoins)

        return Statistics(
            currentCoins = newCoins,
            highestCoins = newHighest,
            wins = newWins,
            losses = newLosses,
            matchesPlayed = newMatches
        )
    }
}

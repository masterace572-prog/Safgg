package com.ludovault.game.engine

import com.ludovault.data.model.Board
import com.ludovault.data.model.GameState
import com.ludovault.data.model.Player
import com.ludovault.data.model.PlayerColor
import com.ludovault.data.model.Token

/**
 * Validates and executes moves according to Ludo rules.
 */
object MoveValidator {

    /**
     * Checks if the current player can roll the dice.
     */
    fun canRoll(state: GameState): Boolean {
        return !state.diceRolled && state.phase != GamePhase.FINISHED
    }

    /**
     * Checks if a specific token can be moved with the current dice value.
     */
    fun canMoveToken(state: GameState, tokenId: Int): Boolean {
        val player = state.currentPlayer()
        val token = player.tokens.find { it.id == tokenId } ?: return false

        if (token.isFinished) return false

        // Need a 6 to leave home
        if (token.isHome()) {
            return state.diceValue == 6
        }

        // Check if token can finish or move within bounds
        val newPos = token.position + state.diceValue
        val homePathPos = Board.getHomePathPosition(player.color, newPos)

        // If entering home path, check if position is valid
        if (homePathPos >= 0) {
            return homePathPos < Board.HOME_PATH_LENGTH
        }

        // Otherwise must stay within outer path until ready for home
        return newPos < Board.PATH_LENGTH + Board.HOME_PATH_LENGTH
    }

    /**
     * Gets all tokens that can be moved in the current state.
     */
    fun getMovableTokens(state: GameState): List<Int> {
        return state.currentPlayer().tokens
            .filter { canMoveToken(state, it.id) }
            .map { it.id }
    }

    /**
     * Executes a move and returns the new game state.
     */
    fun executeMove(state: GameState, tokenId: Int): GameState {
        val player = state.currentPlayer()
        val token = player.tokens.find { it.id == tokenId } ?: return state

        var newPosition = token.position
        var newFinished = token.isFinished
        var capture = false

        if (token.isHome()) {
            // Leave home, enter board at position 0
            newPosition = 0
        } else {
            val targetPos = token.position + state.diceValue
            val homePathPos = Board.getHomePathPosition(player.color, targetPos)

            if (homePathPos >= 0) {
                if (homePathPos >= Board.HOME_PATH_LENGTH) {
                    // Overshot, invalid (should be filtered by canMove)
                    return state
                }
                if (homePathPos == Board.HOME_PATH_LENGTH - 1) {
                    newFinished = true
                }
                newPosition = targetPos
            } else {
                newPosition = targetPos
            }
        }

        // Update token
        val updatedTokens = player.tokens.map {
            if (it.id == tokenId) {
                Token(id = it.id, position = newPosition, isFinished = newFinished)
            } else it
        }

        // Check for capture on outer path only
        var updatedPlayers = state.players.map {
            if (it.color == player.color) {
                it.copy(tokens = updatedTokens)
            } else it
        }

        if (!newFinished && token.position != -1) {
            val globalTile = Board.toGlobalTile(player.color, newPosition)
            if (!Board.isSafeTile(globalTile)) {
                // Check collision with opponents
                val opponents = updatedPlayers.filter { it.color != player.color }
                for (opp in opponents) {
                    for (oppToken in opp.tokens) {
                        if (!oppToken.isHome() && !oppToken.isFinished) {
                            val oppGlobal = Board.toGlobalTile(opp.color, oppToken.position)
                            if (oppGlobal == globalTile) {
                                // Capture!
                                capture = true
                                val capturedTokens = opp.tokens.map { ot ->
                                    if (ot.id == oppToken.id) Token(id = ot.id, position = -1, isFinished = false)
                                    else ot
                                }
                                updatedPlayers = updatedPlayers.map { p ->
                                    if (p.color == opp.color) p.copy(tokens = capturedTokens) else p
                                }
                            }
                        }
                    }
                }
            }
        }

        val nextPlayerIndex = if (state.diceValue == 6 && state.consecutiveSixes < 2) {
            state.currentPlayerIndex // Extra turn for six
        } else {
            (state.currentPlayerIndex + 1) % state.players.size
        }

        val newConsecutive = if (state.diceValue == 6) state.consecutiveSixes + 1 else 0

        // Check win
        val updatedPlayer = updatedPlayers.find { it.color == player.color }!!
        val winner = if (updatedPlayer.hasWon()) player.color else null

        return state.copy(
            players = updatedPlayers,
            currentPlayerIndex = if (winner != null) state.currentPlayerIndex else nextPlayerIndex,
            diceValue = 0,
            diceRolled = false,
            selectedTokenId = -1,
            phase = if (winner != null) GamePhase.WIN_ANIMATION else GamePhase.ROLLING,
            winnerColor = winner,
            lastMoveCapture = capture,
            consecutiveSixes = newConsecutive
        )
    }

    /**
     * Checks if any move is possible after rolling.
     */
    fun hasAnyMove(state: GameState): Boolean {
        return getMovableTokens(state).isNotEmpty()
    }
}

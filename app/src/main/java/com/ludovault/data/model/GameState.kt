package com.ludovault.data.model

/**
 * Represents the current state of a Ludo match.
 *
 * @param players List of four players.
 * @param currentPlayerIndex Index of the player whose turn it is.
 * @param diceValue Last rolled dice value (1-6), 0 if not rolled.
 * @param diceRolled Whether the current player has rolled the dice.
 * @param selectedTokenId The token currently selected for movement, or -1.
 * @param phase Current game phase.
 * @param winnerColor Set when someone wins.
 * @param stake The coin stake for this match.
 * @param lastMoveCapture Whether the last move resulted in a capture.
 * @param consecutiveSixes Number of consecutive sixes rolled by current player.
 */
data class GameState(
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val diceValue: Int = 0,
    val diceRolled: Boolean = false,
    val selectedTokenId: Int = -1,
    val phase: GamePhase = GamePhase.NOT_STARTED,
    val winnerColor: PlayerColor? = null,
    val stake: Int = 0,
    val lastMoveCapture: Boolean = false,
    val consecutiveSixes: Int = 0
) {
    /**
     * Returns the current player.
     */
    fun currentPlayer(): Player = players[currentPlayerIndex]

    /**
     * Returns true if the game has a winner.
     */
    fun isGameOver(): Boolean = winnerColor != null
}

/**
 * Phases of the game lifecycle.
 */
enum class GamePhase {
    NOT_STARTED,
    ROLLING,
    SELECTING_TOKEN,
    MOVING,
    CAPTURE_ANIMATION,
    WIN_ANIMATION,
    FINISHED
}

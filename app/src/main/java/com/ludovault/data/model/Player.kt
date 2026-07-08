package com.ludovault.data.model

/**
 * Represents a Ludo player (human or bot).
 *
 * @param color The player's color.
 * @param type Human or Bot.
 * @param tokens The four tokens owned by this player.
 * @param isActive Whether this player is still in the game.
 */
data class Player(
    val color: PlayerColor,
    val type: PlayerType,
    val tokens: List<Token> = List(4) { Token(id = it) },
    val isActive: Boolean = true
) {
    /**
     * Returns the number of finished tokens.
     */
    fun finishedCount(): Int = tokens.count { it.isFinished }

    /**
     * Returns true if all tokens have finished (player won).
     */
    fun hasWon(): Boolean = tokens.all { it.isFinished }

    /**
     * Returns tokens that are currently on the board (not home, not finished).
     */
    fun tokensOnBoard(): List<Token> = tokens.filter { !it.isHome() && !it.isFinished }

    /**
     * Returns tokens that are in home.
     */
    fun tokensInHome(): List<Token> = tokens.filter { it.isHome() }
}

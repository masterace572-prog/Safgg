package com.ludovault.data.model

/**
 * Represents a single token on the Ludo board.
 *
 * @param id Unique token id within the player (0-3)
 * @param position Current board position. -1 means home (not yet entered).
 * @param isFinished True if the token has reached the end.
 */
data class Token(
    val id: Int,
    val position: Int = -1,
    val isFinished: Boolean = false
) {
    /**
     * Returns true if the token is still in the home area.
     */
    fun isHome(): Boolean = position == -1 && !isFinished

    /**
     * Returns a copy of this token moved by [steps].
     */
    fun move(steps: Int): Token = copy(position = position + steps)
}

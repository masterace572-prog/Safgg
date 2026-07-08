package com.ludovault.data.model

/**
 * Ludo board configuration and path calculations.
 *
 * The board consists of a shared outer path of 52 tiles.
 * Each player has a home path of 5 tiles leading to the center.
 */
object Board {

    const val PATH_LENGTH = 52
    const val HOME_PATH_LENGTH = 5
    const val SAFE_TILES_COUNT = 8

    /**
     * Starting positions on the shared path for each color.
     */
    val START_POSITIONS = mapOf(
        PlayerColor.RED to 0,
        PlayerColor.BLUE to 13,
        PlayerColor.YELLOW to 26,
        PlayerColor.GREEN to 39
    )

    /**
     * Safe tile indices on the shared path where tokens cannot be captured.
     */
    val SAFE_TILES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    /**
     * Converts a token's path-relative position to a global board tile index.
     *
     * @param color Player color.
     * @param position Position along the player's path (0-51 for outer path).
     * @return Global tile index (0-51).
     */
    fun toGlobalTile(color: PlayerColor, position: Int): Int {
        val start = START_POSITIONS[color] ?: 0
        return (start + position) % PATH_LENGTH
    }

    /**
     * Checks if a global tile index is safe.
     */
    fun isSafeTile(globalTile: Int): Boolean = globalTile in SAFE_TILES

    /**
     * Checks if a token can enter the home path.
     * This happens when the token has completed the full outer circle.
     */
    fun canEnterHomePath(color: PlayerColor, position: Int): Boolean {
        val start = START_POSITIONS[color] ?: 0
        // After completing 50 steps on outer path, next step enters home
        return position >= PATH_LENGTH - 2
    }

    /**
     * Gets the home path position (0-5) after entering from outer path.
     * Returns -1 if not yet in home path.
     */
    fun getHomePathPosition(color: PlayerColor, position: Int): Int {
        val start = START_POSITIONS[color] ?: 0
        val effective = position - (PATH_LENGTH - 2)
        return if (effective >= 0 && effective < HOME_PATH_LENGTH) effective else -1
    }
}

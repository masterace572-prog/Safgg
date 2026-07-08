package com.ludovault.game.engine

import com.ludovault.data.model.Board
import com.ludovault.data.model.PlayerColor

/**
 * Pre-computed board geometry for rendering and logic.
 */
object BoardConfig {

    /**
     * Represents a coordinate on the board grid.
     */
    data class Coord(val x: Int, val y: Int)

    private const val GRID_SIZE = 15

    /**
     * Home area top-left corners for each color.
     */
    val HOME_AREAS = mapOf(
        PlayerColor.RED to Coord(0, 0),
        PlayerColor.GREEN to Coord(9, 0),
        PlayerColor.YELLOW to Coord(9, 9),
        PlayerColor.BLUE to Coord(0, 9)
    )

    /**
     * Home positions for tokens (relative to home area top-left).
     */
    val HOME_OFFSETS = listOf(
        Coord(1, 1), Coord(4, 1),
        Coord(1, 4), Coord(4, 4)
    )

    /**
     * The outer path coordinates in order (52 tiles).
     */
    val OUTER_PATH: List<Coord> = listOf(
        // Red to Blue (13)
        Coord(6, 1), Coord(6, 2), Coord(6, 3), Coord(6, 4), Coord(6, 5), Coord(6, 6),
        Coord(5, 6), Coord(4, 6), Coord(3, 6), Coord(2, 6), Coord(1, 6), Coord(0, 6),
        Coord(0, 7),
        // Blue to Yellow (13)
        Coord(0, 8), Coord(1, 8), Coord(2, 8), Coord(3, 8), Coord(4, 8), Coord(5, 8),
        Coord(6, 8), Coord(6, 9), Coord(6, 10), Coord(6, 11), Coord(6, 12), Coord(6, 13),
        Coord(7, 13),
        // Yellow to Green (13)
        Coord(8, 13), Coord(8, 12), Coord(8, 11), Coord(8, 10), Coord(8, 9), Coord(8, 8),
        Coord(9, 8), Coord(10, 8), Coord(11, 8), Coord(12, 8), Coord(13, 8), Coord(13, 7),
        Coord(13, 6),
        // Green to Red (13)
        Coord(12, 6), Coord(11, 6), Coord(10, 6), Coord(9, 6), Coord(8, 6), Coord(8, 5),
        Coord(8, 4), Coord(8, 3), Coord(8, 2), Coord(8, 1), Coord(8, 0), Coord(7, 0),
        Coord(6, 0)
    )

    /**
     * Home path coordinates for each color (5 tiles leading to center).
     */
    val HOME_PATHS: Map<PlayerColor, List<Coord>> = mapOf(
        PlayerColor.RED to buildList {
            for (i in 0..4) add(Coord(7, 1 + i))
        },
        PlayerColor.GREEN to buildList {
            for (i in 0..4) add(Coord(11 - i, 7))
        },
        PlayerColor.YELLOW to buildList {
            for (i in 0..4) add(Coord(7, 11 - i))
        },
        PlayerColor.BLUE to buildList {
            for (i in 0..4) add(Coord(1 + i, 7))
        }
    )

    /**
     * Center coordinate.
     */
    val CENTER = Coord(7, 7)

    /**
     * Gets the render coordinate for a token based on its state.
     */
    fun getTokenCoord(color: PlayerColor, tokenId: Int, position: Int, isFinished: Boolean): Coord {
        if (isFinished) {
            // Finished tokens stack near center
            return Coord(CENTER.x + (tokenId % 2), CENTER.y + (tokenId / 2))
        }
        if (position == -1) {
            val home = HOME_AREAS[color] ?: Coord(0, 0)
            val off = HOME_OFFSETS[tokenId]
            return Coord(home.x + off.x, home.y + off.y)
        }
        val global = Board.toGlobalTile(color, position)
        return OUTER_PATH[global]
    }

    /**
     * Gets the coordinate for a token in the home path.
     */
    fun getHomePathCoord(color: PlayerColor, homePos: Int): Coord {
        return HOME_PATHS[color]?.get(homePos) ?: CENTER
    }
}

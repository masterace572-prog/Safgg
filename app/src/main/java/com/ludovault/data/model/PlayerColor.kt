package com.ludovault.data.model

import androidx.compose.ui.graphics.Color

/**
 * Represents the four Ludo player colors.
 */
enum class PlayerColor(val displayName: String, val color: Color) {
    RED("Red", Color(0xFFE53935)),
    GREEN("Green", Color(0xFF43A047)),
    YELLOW("Yellow", Color(0xFFFDD835)),
    BLUE("Blue", Color(0xFF1E88E5));

    companion object {
        fun fromOrdinal(ordinal: Int): PlayerColor = entries[ordinal]
    }
}

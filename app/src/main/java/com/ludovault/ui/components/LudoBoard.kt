package com.ludovault.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ludovault.data.model.Board
import com.ludovault.data.model.GameState
import com.ludovault.data.model.PlayerColor
import com.ludovault.data.model.PlayerType
import com.ludovault.game.engine.BoardConfig

/**
 * Renders the complete Ludo board with tokens.
 */
@Composable
fun LudoBoard(
    gameState: GameState,
    onTokenClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val tileSize = with(LocalDensity.current) {
            val width = maxOf(1, (maxWidth - 32.dp).roundToPx())
            (width / 15).toDp()
        }

        // Grid background
        BoardBackground(tileSize)

        // Tokens
        gameState.players.forEach { player ->
            player.tokens.forEach { token ->
                val isSelectable = gameState.currentPlayer().type == PlayerType.HUMAN &&
                        gameState.phase == com.ludovault.data.model.GamePhase.SELECTING_TOKEN &&
                        player.color == gameState.currentPlayer().color &&
                        com.ludovault.game.engine.MoveValidator.canMoveToken(gameState, token.id)

                val isSelected = gameState.selectedTokenId == token.id

                TokenView(
                    color = player.color,
                    tokenId = token.id,
                    position = token.position,
                    isFinished = token.isFinished,
                    tileSize = tileSize,
                    isSelectable = isSelectable,
                    isSelected = isSelected,
                    onClick = { onTokenClick(token.id) }
                )
            }
        }
    }
}

@Composable
private fun BoardBackground(tileSize: Dp) {
    val colors = mapOf(
        PlayerColor.RED to Color(0xFFFFCDD2),
        PlayerColor.GREEN to Color(0xFFC8E6C9),
        PlayerColor.YELLOW to Color(0xFFFFF9C4),
        PlayerColor.BLUE to Color(0xFFBBDEFB)
    )

    // Home areas
    BoardConfig.HOME_AREAS.forEach { (color, coord) ->
        Box(
            modifier = Modifier
                .offset(
                    x = tileSize * coord.x,
                    y = tileSize * coord.y
                )
                .size(tileSize * 6)
                .background(colors[color] ?: Color.Gray)
                .border(1.dp, color.copy(alpha = 0.5f))
        )

        // Home positions
        BoardConfig.HOME_OFFSETS.forEach { off ->
            Box(
                modifier = Modifier
                    .offset(
                        x = tileSize * (coord.x + off.x),
                        y = tileSize * (coord.y + off.y)
                    )
                    .size(tileSize)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }

    // Outer path tiles
    BoardConfig.OUTER_PATH.forEachIndexed { index, coord ->
        val isSafe = Board.SAFE_TILES.contains(index)
        Box(
            modifier = Modifier
                .offset(x = tileSize * coord.x, y = tileSize * coord.y)
                .size(tileSize)
                .padding(1.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isSafe) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )
                .border(
                    width = if (isSafe) 1.5.dp else 0.5.dp,
                    color = if (isSafe) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }

    // Home paths
    BoardConfig.HOME_PATHS.forEach { (color, coords) ->
        coords.forEach { coord ->
            Box(
                modifier = Modifier
                    .offset(x = tileSize * coord.x, y = tileSize * coord.y)
                    .size(tileSize)
                    .padding(1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.3f))
                    .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
        }
    }

    // Center
    Box(
        modifier = Modifier
            .offset(
                x = tileSize * BoardConfig.CENTER.x,
                y = tileSize * BoardConfig.CENTER.y
            )
            .size(tileSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    )
}

@Composable
private fun TokenView(
    color: PlayerColor,
    tokenId: Int,
    position: Int,
    isFinished: Boolean,
    tileSize: Dp,
    isSelectable: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val coord = if (isFinished) {
        BoardConfig.getTokenCoord(color, tokenId, position, true)
    } else if (position >= 0) {
        val homePathPos = Board.getHomePathPosition(color, position)
        if (homePathPos >= 0) {
            BoardConfig.getHomePathCoord(color, homePathPos)
        } else {
            BoardConfig.getTokenCoord(color, tokenId, position, false)
        }
    } else {
        BoardConfig.getTokenCoord(color, tokenId, position, false)
    }

    val tokenColor = when (color) {
        PlayerColor.RED -> Color(0xFFE53935)
        PlayerColor.GREEN -> Color(0xFF43A047)
        PlayerColor.YELLOW -> Color(0xFFFDD835)
        PlayerColor.BLUE -> Color(0xFF1E88E5)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.3f else if (isSelectable) 1.15f else 1f,
        animationSpec = tween(200),
        label = "token_scale"
    )

    Box(
        modifier = Modifier
            .offset(
                x = tileSize * coord.x + tileSize * 0.1f,
                y = tileSize * coord.y + tileSize * 0.1f
            )
            .size(tileSize * 0.8f)
            .scale(scale)
            .clip(CircleShape)
            .background(tokenColor)
            .then(
                if (isSelectable) {
                    Modifier.clickable(onClick = onClick)
                        .border(2.dp, Color.White, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(tileSize * 0.3f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}

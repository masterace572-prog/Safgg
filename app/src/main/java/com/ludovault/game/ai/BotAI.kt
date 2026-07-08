package com.ludovault.game.ai

import com.ludovault.data.model.Board
import com.ludovault.data.model.GameState
import com.ludovault.data.model.Player
import com.ludovault.data.model.PlayerColor
import com.ludovault.data.model.Token
import com.ludovault.game.engine.MoveValidator

/**
 * Rule-based AI for bot players.
 */
object BotAI {

    /**
     * Chooses the best token to move for the current bot player.
     *
     * @param state Current game state.
     * @return Token ID to move, or -1 if no move possible.
     */
    fun chooseMove(state: GameState): Int {
        val movable = MoveValidator.getMovableTokens(state)
        if (movable.isEmpty()) return -1

        val player = state.currentPlayer()
        val dice = state.diceValue

        // Priority 1: Capture opponent token
        movable.forEach { tokenId ->
            if (canCapture(state, player, tokenId, dice)) {
                return tokenId
            }
        }

        // Priority 2: Leave home on a 6
        if (dice == 6) {
            val homeToken = movable.find { tokenId ->
                player.tokens.find { it.id == tokenId }?.isHome() == true
            }
            if (homeToken != null) return homeToken
        }

        // Priority 3: Move token that can reach home/finish
        movable.forEach { tokenId ->
            if (canFinish(state, player, tokenId, dice)) {
                return tokenId
            }
        }

        // Priority 4: Move token closest to home path
        val closestToHome = movable.maxByOrNull { tokenId ->
            val token = player.tokens.find { it.id == tokenId } ?: return@maxByOrNull -1
            if (token.isHome()) -1 else token.position
        }
        if (closestToHome != null && player.tokens.find { it.id == closestToHome }?.position?.let { it > 40 } == true) {
            return closestToHome
        }

        // Priority 5: Move token to a safe tile
        val safeMove = movable.find { tokenId ->
            landsOnSafeTile(state, player, tokenId, dice)
        }
        if (safeMove != null) return safeMove

        // Priority 6: Avoid danger (don't move tokens that would land where opponent can capture)
        val dangerous = movable.filter { tokenId ->
            isDangerousMove(state, player, tokenId, dice)
        }
        val safeOptions = movable - dangerous.toSet()
        if (safeOptions.isNotEmpty()) {
            // Move the token farthest from home (advance the lagging token)
            return safeOptions.maxByOrNull { tokenId ->
                val token = player.tokens.find { it.id == tokenId }
                token?.position ?: -1
            } ?: safeOptions.first()
        }

        // Priority 7: Move the token that is farthest along
        return movable.maxByOrNull { tokenId ->
            val token = player.tokens.find { it.id == tokenId }
            token?.position ?: -1
        } ?: movable.first()
    }

    private fun canCapture(state: GameState, player: Player, tokenId: Int, dice: Int): Boolean {
        val token = player.tokens.find { it.id == tokenId } ?: return false
        val newPos = if (token.isHome()) 0 else token.position + dice
        if (token.isHome() && dice != 6) return false

        val homePathPos = Board.getHomePathPosition(player.color, newPos)
        if (homePathPos >= 0) return false // Can't capture in home path

        val globalTile = Board.toGlobalTile(player.color, newPos)
        if (Board.isSafeTile(globalTile)) return false

        return state.players.any { opp ->
            opp.color != player.color && opp.tokens.any { oppToken ->
                !oppToken.isHome() && !oppToken.isFinished &&
                        Board.toGlobalTile(opp.color, oppToken.position) == globalTile
            }
        }
    }

    private fun canFinish(state: GameState, player: Player, tokenId: Int, dice: Int): Boolean {
        val token = player.tokens.find { it.id == tokenId } ?: return false
        if (token.isHome()) return false
        val targetPos = token.position + dice
        val homePathPos = Board.getHomePathPosition(player.color, targetPos)
        return homePathPos == Board.HOME_PATH_LENGTH - 1
    }

    private fun landsOnSafeTile(state: GameState, player: Player, tokenId: Int, dice: Int): Boolean {
        val token = player.tokens.find { it.id == tokenId } ?: return false
        val newPos = if (token.isHome()) 0 else token.position + dice
        if (token.isHome() && dice != 6) return false
        val homePathPos = Board.getHomePathPosition(player.color, newPos)
        if (homePathPos >= 0) return true // Home path is safe
        val globalTile = Board.toGlobalTile(player.color, newPos)
        return Board.isSafeTile(globalTile)
    }

    private fun isDangerousMove(state: GameState, player: Player, tokenId: Int, dice: Int): Boolean {
        val token = player.tokens.find { it.id == tokenId } ?: return false
        val newPos = if (token.isHome()) 0 else token.position + dice
        if (token.isHome() && dice != 6) return false
        val homePathPos = Board.getHomePathPosition(player.color, newPos)
        if (homePathPos >= 0) return false // Home path is safe

        val globalTile = Board.toGlobalTile(player.color, newPos)
        if (Board.isSafeTile(globalTile)) return false

        // Check if any opponent is within 6 tiles behind and could capture
        return state.players.any { opp ->
            opp.color != player.color && opp.tokens.any { oppToken ->
                if (oppToken.isHome() || oppToken.isFinished) return@any false
                val oppGlobal = Board.toGlobalTile(opp.color, oppToken.position)
                val distance = (globalTile - oppGlobal + Board.PATH_LENGTH) % Board.PATH_LENGTH
                distance in 1..6
            }
        }
    }
}

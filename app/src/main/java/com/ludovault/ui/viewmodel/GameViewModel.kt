package com.ludovault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludovault.data.model.GamePhase
import com.ludovault.data.model.GameState
import com.ludovault.data.model.PlayerType
import com.ludovault.data.model.Statistics
import com.ludovault.data.repository.GameRepository
import com.ludovault.game.ai.BotAI
import com.ludovault.game.engine.GameEngine
import com.ludovault.game.engine.MoveValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the Ludo game lifecycle and state.
 */
class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val engine = GameEngine()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _diceAnimating = MutableStateFlow(false)
    val diceAnimating: StateFlow<Boolean> = _diceAnimating.asStateFlow()

    private val _showCapture = MutableStateFlow(false)
    val showCapture: StateFlow<Boolean> = _showCapture.asStateFlow()

    private val _matchFinished = MutableStateFlow(false)
    val matchFinished: StateFlow<Boolean> = _matchFinished.asStateFlow()

    private val _matchResult = MutableStateFlow<Statistics?>(null)
    val matchResult: StateFlow<Statistics?> = _matchResult.asStateFlow()

    /**
     * Starts a new game with the given stake.
     */
    fun startGame(stake: Int) {
        _gameState.value = engine.createGame(stake)
        _matchFinished.value = false
        _matchResult.value = null
        processBotTurn()
    }

    /**
     * Rolls the dice for the human player.
     */
    fun rollDice() {
        val state = _gameState.value
        if (state.currentPlayer().type != PlayerType.HUMAN) return

        viewModelScope.launch {
            _diceAnimating.value = true
            delay(600)
            val (newState, value) = engine.rollDice(state)
            _diceAnimating.value = false
            _gameState.value = newState

            if (newState.phase == GamePhase.SELECTING_TOKEN && !MoveValidator.hasAnyMove(newState)) {
                delay(800)
                passTurn()
            }
        }
    }

    /**
     * Selects a token for the human player.
     */
    fun selectToken(tokenId: Int) {
        val state = _gameState.value
        if (state.currentPlayer().type != PlayerType.HUMAN) return
        val newState = engine.selectToken(state, tokenId)
        _gameState.value = newState

        if (newState.phase == GamePhase.MOVING) {
            viewModelScope.launch {
                delay(300)
                confirmMove()
            }
        }
    }

    /**
     * Confirms the current move.
     */
    private fun confirmMove() {
        val state = _gameState.value
        val newState = engine.confirmMove(state)
        _gameState.value = newState

        if (newState.lastMoveCapture) {
            viewModelScope.launch {
                _showCapture.value = true
                delay(800)
                _showCapture.value = false
            }
        }

        if (newState.isGameOver()) {
            finishMatch()
        } else {
            viewModelScope.launch {
                delay(600)
                processBotTurn()
            }
        }
    }

    /**
     * Passes turn when no moves available.
     */
    private fun passTurn() {
        val state = _gameState.value
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        _gameState.value = state.copy(
            currentPlayerIndex = nextIndex,
            diceValue = 0,
            diceRolled = false,
            phase = GamePhase.ROLLING,
            consecutiveSixes = 0
        )
        processBotTurn()
    }

    /**
     * Processes bot turns automatically.
     */
    private fun processBotTurn() {
        viewModelScope.launch {
            while (_gameState.value.currentPlayer().type == PlayerType.BOT && !_gameState.value.isGameOver()) {
                delay(800)
                val state = _gameState.value
                _diceAnimating.value = true
                delay(600)
                val (rolledState, _) = engine.rollDice(state)
                _diceAnimating.value = false
                _gameState.value = rolledState

                if (rolledState.phase == GamePhase.SELECTING_TOKEN) {
                    val tokenId = BotAI.chooseMove(rolledState)
                    if (tokenId != -1) {
                        delay(400)
                        val selected = engine.selectToken(rolledState, tokenId)
                        _gameState.value = selected
                        delay(400)
                        val moved = engine.confirmMove(selected)
                        _gameState.value = moved

                        if (moved.lastMoveCapture) {
                            _showCapture.value = true
                            delay(600)
                            _showCapture.value = false
                        }

                        if (moved.isGameOver()) {
                            finishMatch()
                            return@launch
                        }
                        delay(500)
                    } else {
                        delay(500)
                        val nextIndex = (rolledState.currentPlayerIndex + 1) % rolledState.players.size
                        _gameState.value = rolledState.copy(
                            currentPlayerIndex = nextIndex,
                            diceValue = 0,
                            diceRolled = false,
                            phase = GamePhase.ROLLING,
                            consecutiveSixes = 0
                        )
                    }
                } else {
                    delay(500)
                    val nextIndex = (rolledState.currentPlayerIndex + 1) % rolledState.players.size
                    _gameState.value = rolledState.copy(
                        currentPlayerIndex = nextIndex,
                        diceValue = 0,
                        diceRolled = false,
                        phase = GamePhase.ROLLING,
                        consecutiveSixes = 0
                    )
                }
            }
        }
    }

    /**
     * Finishes the match and updates statistics.
     */
    private fun finishMatch() {
        viewModelScope.launch {
            delay(1500)
            val state = _gameState.value
            val currentStats = repository.statistics.first()
            val newStats = engine.calculateMatchResult(state, currentStats)
            repository.saveStatistics(newStats)
            _matchResult.value = newStats
            _matchFinished.value = true
        }
    }

    /**
     * Resets the match state without saving.
     */
    fun resetMatch() {
        _gameState.value = GameState()
        _matchFinished.value = false
        _matchResult.value = null
        _showCapture.value = false
        _diceAnimating.value = false
    }
}

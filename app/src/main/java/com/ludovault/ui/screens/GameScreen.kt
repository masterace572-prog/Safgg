package com.ludovault.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ludovault.data.model.GamePhase
import com.ludovault.data.model.PlayerColor
import com.ludovault.data.model.PlayerType
import com.ludovault.ui.components.LudoBoard
import com.ludovault.ui.viewmodel.GameViewModel

/**
 * Main game screen with board, dice, and controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    stake: Int,
    onExit: () -> Unit,
    onOutOfCoins: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val diceAnimating by viewModel.diceAnimating.collectAsStateWithLifecycle()
    val showCapture by viewModel.showCapture.collectAsStateWithLifecycle()
    val matchFinished by viewModel.matchFinished.collectAsStateWithLifecycle()
    val matchResult by viewModel.matchResult.collectAsStateWithLifecycle()

    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(stake) {
        viewModel.startGame(stake)
    }

    LaunchedEffect(matchResult) {
        if (matchResult != null && matchResult!!.currentCoins < 50) {
            onOutOfCoins()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Match")
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "Stake: $stake",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Turn indicator
                TurnIndicator(gameState)

                Spacer(modifier = Modifier.height(8.dp))

                // Board
                Box(modifier = Modifier.weight(1f)) {
                    LudoBoard(
                        gameState = gameState,
                        onTokenClick = { tokenId ->
                            if (gameState.currentPlayer().type == PlayerType.HUMAN) {
                                viewModel.selectToken(tokenId)
                            }
                        }
                    )

                    // Capture overlay
                    AnimatedVisibility(
                        visible = showCapture,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Captured!",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dice and controls
                DiceControls(
                    gameState = gameState,
                    diceAnimating = diceAnimating,
                    onRoll = { viewModel.rollDice() }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Match finished overlay
            if (matchFinished) {
                MatchResultOverlay(
                    isWin = gameState.winnerColor == PlayerColor.RED,
                    stake = stake,
                    onHome = {
                        viewModel.resetMatch()
                        onExit()
                    }
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave Match?") },
            text = { Text("Your stake will be lost if you leave now.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    viewModel.resetMatch()
                    onExit()
                }) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }
}

@Composable
private fun TurnIndicator(gameState: GameState) {
    val current = gameState.currentPlayer()
    val color = when (current.color) {
        PlayerColor.RED -> Color(0xFFE53935)
        PlayerColor.GREEN -> Color(0xFF43A047)
        PlayerColor.YELLOW -> Color(0xFFFDD835)
        PlayerColor.BLUE -> Color(0xFF1E88E5)
    }
    val isHuman = current.type == PlayerType.HUMAN

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isHuman) "Your Turn" else "${current.color.displayName} Bot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DiceControls(
    gameState: GameState,
    diceAnimating: Boolean,
    onRoll: () -> Unit
) {
    val isHumanTurn = gameState.currentPlayer().type == PlayerType.HUMAN
    val canRoll = isHumanTurn && !gameState.diceRolled && gameState.phase != GamePhase.FINISHED

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Dice display
        val rotation by animateFloatAsState(
            targetValue = if (diceAnimating) 360f else 0f,
            animationSpec = tween(600),
            label = "dice_rotation"
        )
        val scale by animateFloatAsState(
            targetValue = if (diceAnimating) 1.2f else 1f,
            animationSpec = tween(300),
            label = "dice_scale"
        )

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = canRoll && !diceAnimating, onClick = onRoll),
            contentAlignment = Alignment.Center
        ) {
            if (gameState.diceValue > 0 && !diceAnimating) {
                Text(
                    text = gameState.diceValue.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(scale)
                )
            } else if (diceAnimating) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Rolling",
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Dice",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (canRoll) {
            Text(
                text = "Tap to Roll",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (gameState.diceRolled && isHumanTurn) {
            Text(
                text = "Select a token",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        } else if (!isHumanTurn) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bot thinking...",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MatchResultOverlay(
    isWin: Boolean,
    stake: Int,
    onHome: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isWin) Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else Color(0xFFE53935).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWin) "🏆" else "💔",
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isWin) "You Won!" else "You Lost!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isWin) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isWin) "+${stake * 2} coins" else "-${stake} coins",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

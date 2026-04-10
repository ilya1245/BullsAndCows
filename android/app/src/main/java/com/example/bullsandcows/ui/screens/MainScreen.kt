package com.example.bullsandcows.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.bullsandcows.model.GamePhase
import com.example.bullsandcows.ui.components.*
import com.example.bullsandcows.viewmodel.GameViewModel
import kotlinx.coroutines.launch

enum class KbTarget { LEFT, RIGHT_BULLS, RIGHT_COWS }

@Composable
fun MainScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    val isRunning   = state.phase == GamePhase.RUNNING
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    // ---- Lifted input state ----
    var leftInput  by remember { mutableStateOf("") }
    var rightBulls by remember { mutableStateOf("") }
    var rightCows  by remember { mutableStateOf("") }
    var kbTarget   by remember { mutableStateOf(KbTarget.LEFT) }
    var kbVisible  by remember { mutableStateOf(false) }

    val numSize = state.settings.numSize

    // Auto-reset inputs when active panel changes
    LaunchedEffect(state.left.inputEnabled) {
        if (state.left.inputEnabled) {
            leftInput = ""
            kbTarget  = KbTarget.LEFT
        }
    }
    LaunchedEffect(state.right.inputEnabled) {
        if (state.right.inputEnabled) {
            rightBulls = ""
            rightCows  = ""
            kbTarget   = KbTarget.RIGHT_BULLS
        }
    }
    // Auto-advance to cows after bulls filled
    LaunchedEffect(rightBulls) {
        if (kbTarget == KbTarget.RIGHT_BULLS && rightBulls.length == 1) {
            kbTarget = KbTarget.RIGHT_COWS
        }
    }

    // Hide keyboard when game ends
    LaunchedEffect(state.phase) {
        if (state.phase != GamePhase.RUNNING) kbVisible = false
    }

    fun handleKey(key: Char) {
        when (kbTarget) {
            KbTarget.LEFT -> when (key) {
                '⌫' -> leftInput = leftInput.dropLast(1)
                '?' -> if (leftInput.isEmpty()) leftInput = "?"
                else -> if (leftInput.length < numSize) leftInput += key
            }
            KbTarget.RIGHT_BULLS -> when (key) {
                '⌫'        -> rightBulls = rightBulls.dropLast(1)
                in '0'..'9' -> if (rightBulls.isEmpty()) rightBulls = key.toString()
                else        -> {}
            }
            KbTarget.RIGHT_COWS -> when (key) {
                '⌫'        -> rightCows = rightCows.dropLast(1)
                in '0'..'9' -> if (rightCows.isEmpty()) rightCows = key.toString()
                else        -> {}
            }
        }
    }

    // Close drawer when game starts
    LaunchedEffect(isRunning) {
        if (isRunning) drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerContent(
                settings   = state.settings,
                isRunning  = isRunning,
                onSettings = viewModel::updateSettings,
                onNewGame  = {
                    scope.launch { drawerState.close() }
                    viewModel.startGame()
                },
                onBreak    = {
                    scope.launch { drawerState.close() }
                    viewModel.breakGame()
                },
                onHelp     = { showHelp = true }
            )
        }
    ) {
        val kbForLeft  = kbVisible && state.phase == GamePhase.RUNNING && kbTarget == KbTarget.LEFT
        val kbForRight = kbVisible && state.phase == GamePhase.RUNNING &&
                         (kbTarget == KbTarget.RIGHT_BULLS || kbTarget == KbTarget.RIGHT_COWS)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Column with animateContentSize — smoothly shrinks/grows with keyboard
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    statusSpec  = state.statusSpec
                )

                when (state.phase) {
                    GamePhase.IDLE -> WelcomeArea(modifier = Modifier.weight(1f))
                    else -> GameArea(
                        state        = state,
                        leftInput    = leftInput,
                        rightBulls   = rightBulls,
                        rightCows    = rightCows,
                        kbTarget     = kbTarget,
                        onFieldClick = { target ->
                            kbTarget  = target
                            kbVisible = true
                        },
                        onLeft  = {
                            val v = leftInput.trim()
                            if (v.isNotEmpty()) {
                                viewModel.onLeftMove(v)
                                kbVisible = false
                            }
                        },
                        onRight = { b, c ->
                            viewModel.onRightAnswer(b, c)
                            kbVisible = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Push-up mode: expand/shrink so GameArea smoothly adjusts height
                AnimatedVisibility(
                    visible = kbForRight,
                    enter   = expandVertically(animationSpec = tween(300)),
                    exit    = shrinkVertically(animationSpec = tween(300))
                ) {
                    NumKeyboard(onKey = ::handleKey, onHide = { kbVisible = false })
                }
            }

            // Overlay mode: keyboard floats over bottom for left panel
            AnimatedVisibility(
                visible  = kbForLeft,
                enter    = slideInVertically { it },
                exit     = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NumKeyboard(onKey = ::handleKey, onHide = { kbVisible = false })
            }
        }
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }
}

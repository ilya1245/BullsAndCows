package com.example.bullsandcows.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bullsandcows.model.GamePhase
import com.example.bullsandcows.ui.components.*
import com.example.bullsandcows.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    val isRunning = state.phase == GamePhase.RUNNING
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Закрыть drawer когда игра стартует
    LaunchedEffect(isRunning) {
        if (isRunning) drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopBar(
                onMenuClick = { scope.launch { drawerState.open() } },
                statusText  = state.statusText
            )

            when (state.phase) {
                GamePhase.IDLE -> WelcomeArea()
                else -> GameArea(
                    state    = state,
                    onLeft   = viewModel::onLeftMove,
                    onRight  = viewModel::onRightAnswer
                )
            }
        }
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }
}

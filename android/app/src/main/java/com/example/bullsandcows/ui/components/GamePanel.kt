package com.example.bullsandcows.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bullsandcows.R
import com.example.bullsandcows.model.*
import com.example.bullsandcows.ui.screens.KbTarget

// ---- Welcome area (shown before first game) ----

@Composable
fun WelcomeArea(modifier: Modifier = Modifier) {
    Box(
        modifier            = modifier.fillMaxSize(),
        contentAlignment    = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text      = stringResource(R.string.welcome_message),
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ---- Game area: one or two panels ----

@Composable
fun GameArea(
    state:        UiState,
    leftInput:    String,
    rightBulls:   String,
    rightCows:    String,
    kbTarget:     KbTarget,
    onFieldClick: (KbTarget) -> Unit,
    onLeft:       () -> Unit,
    onRight:      (Int, Int) -> Unit,
    modifier:     Modifier = Modifier
) {
    val showLeft  = state.settings.gameType == 1 || state.settings.gameType == 3
    val showRight = state.settings.gameType == 2 || state.settings.gameType == 3

    if (showLeft && showRight) {
        Column(modifier = modifier.fillMaxSize()) {
            PlayerPanel(
                state        = state.left,
                inputValue   = leftInput,
                kbTarget     = kbTarget,
                onFieldClick = onFieldClick,
                onMove       = onLeft,
                modifier     = Modifier.weight(1f)
            )
            Spacer(Modifier.height(4.dp))
            ComputerPanel(
                state        = state.right,
                bulls        = rightBulls,
                cows         = rightCows,
                kbTarget     = kbTarget,
                onFieldClick = onFieldClick,
                onAnswer     = onRight,
                modifier     = Modifier.weight(1f)
            )
        }
    } else if (showLeft) {
        PlayerPanel(
            state        = state.left,
            inputValue   = leftInput,
            kbTarget     = kbTarget,
            onFieldClick = onFieldClick,
            onMove       = onLeft,
            modifier     = modifier.fillMaxSize()
        )
    } else {
        ComputerPanel(
            state        = state.right,
            bulls        = rightBulls,
            cows         = rightCows,
            kbTarget     = kbTarget,
            onFieldClick = onFieldClick,
            onAnswer     = onRight,
            modifier     = modifier.fillMaxSize()
        )
    }
}

// ---- Left panel: player guesses ----

@Composable
fun PlayerPanel(
    state:        LeftPanelState,
    inputValue:   String,
    kbTarget:     KbTarget,
    onFieldClick: (KbTarget) -> Unit,
    onMove:       () -> Unit,
    modifier:     Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        // Header
        PanelHeader(
            icon     = "🎯",
            title    = stringResource(R.string.panel_left_title)
        )

        // History table
        HistoryTable(
            attempts   = state.attempts,
            moveHeader = stringResource(R.string.col_move),
            modifier   = Modifier.weight(1f)
        )

        // Prompt
        PromptText(prompt = state.prompt)

        // Input row
        LeftInputRow(
            value        = inputValue,
            isActive     = kbTarget == KbTarget.LEFT,
            onActivate   = { onFieldClick(KbTarget.LEFT) },
            enabled      = state.inputEnabled,
            onSubmit     = onMove
        )
    }
}

// ---- Right panel: computer guesses ----

@Composable
fun ComputerPanel(
    state:        RightPanelState,
    bulls:        String,
    cows:         String,
    kbTarget:     KbTarget,
    onFieldClick: (KbTarget) -> Unit,
    onAnswer:     (Int, Int) -> Unit,
    modifier:     Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        // Header
        PanelHeader(
            icon  = "🤖",
            title = stringResource(R.string.panel_right_title)
        )

        // History table
        HistoryTable(
            attempts   = state.attempts,
            moveHeader = stringResource(R.string.col_comp_move),
            modifier   = Modifier.weight(1f)
        )

        // Prompt
        PromptText(prompt = state.prompt)

        // Input row
        RightInputRow(
            bulls        = bulls,
            cows         = cows,
            kbTarget     = kbTarget,
            onFieldClick = onFieldClick,
            enabled      = state.inputEnabled,
            onSubmit     = {
                val b = bulls.trim().toIntOrNull() ?: -1
                val c = cows.trim().toIntOrNull()  ?: -1
                onAnswer(b, c)
            }
        )
    }
}

// ---- Panel header ----

@Composable
private fun PanelHeader(icon: String, title: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(icon, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.width(6.dp))
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- Prompt text ----

@Composable
fun PromptText(prompt: com.example.bullsandcows.model.Prompt, modifier: Modifier = Modifier) {
    if (prompt.isEmpty) return
    val text = if (prompt.args.isEmpty())
        androidx.compose.ui.res.stringResource(prompt.resId)
    else
        androidx.compose.ui.res.stringResource(prompt.resId, *prompt.args.toTypedArray())
    val color = when (prompt.type) {
        PromptType.SUCCESS -> com.example.bullsandcows.ui.theme.bullColor()
        PromptType.WARN    -> MaterialTheme.colorScheme.tertiary
        PromptType.ERROR   -> MaterialTheme.colorScheme.error
        PromptType.MUTED   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        PromptType.NORMAL  -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text     = text,
        style    = MaterialTheme.typography.bodyMedium,
        color    = color,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

package com.example.bullsandcows.model

import androidx.annotation.StringRes

// ---- Top-level game phase ----

enum class GamePhase { IDLE, RUNNING, FINISHED }

// ---- Prompt (stores resource ID + args; resolved in composable) ----

enum class PromptType { NORMAL, SUCCESS, WARN, ERROR, MUTED }

data class Prompt(
    @StringRes val resId: Int = 0,
    val args: List<Any> = emptyList(),
    val type: PromptType = PromptType.NORMAL
) {
    val isEmpty get() = resId == 0
    companion object {
        val Empty = Prompt()
    }
}

// ---- Status bar spec (resolved in composable) ----

sealed class StatusSpec {
    object Configure : StatusSpec()
    data class Running(val settings: GameSettings) : StatusSpec()
    object Broken : StatusSpec()
}

// ---- Single move attempt ----

data class Attempt(
    val number: String,
    val bulls: Int,
    val cows: Int,
    val isWin: Boolean = false,
    val isReveal: Boolean = false
)

// ---- Panel states ----

data class LeftPanelState(
    val compNumber: String = "",
    val attempts: List<Attempt> = emptyList(),
    val won: Boolean = false,
    val practicing: Boolean = false,
    val inputEnabled: Boolean = false,
    val prompt: Prompt = Prompt.Empty
)

data class RightPanelState(
    val currentMove: String = "",
    val attempts: List<Attempt> = emptyList(),
    val won: Boolean = false,
    val practicing: Boolean = false,
    val inputEnabled: Boolean = false,
    val prompt: Prompt = Prompt.Empty
)

// ---- Root UI state ----

data class UiState(
    val settings: GameSettings = GameSettings(),
    val phase: GamePhase = GamePhase.IDLE,
    val statusSpec: StatusSpec = StatusSpec.Configure,
    val left: LeftPanelState = LeftPanelState(),
    val right: RightPanelState = RightPanelState()
)

package com.example.bullsandcows.model

// ---- Top-level game phase ----

enum class GamePhase { IDLE, RUNNING, FINISHED }

// ---- Prompt ----

enum class PromptType { NORMAL, SUCCESS, WARN, ERROR, MUTED }

data class Prompt(
    val text: String = "",
    val type: PromptType = PromptType.NORMAL
) {
    companion object {
        val Empty = Prompt()
    }
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
    val statusText: String = "",
    val left: LeftPanelState = LeftPanelState(),
    val right: RightPanelState = RightPanelState()
)

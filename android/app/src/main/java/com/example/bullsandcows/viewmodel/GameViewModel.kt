package com.example.bullsandcows.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullsandcows.R
import com.example.bullsandcows.logic.ComputerPlayer
import com.example.bullsandcows.logic.GameNumber
import com.example.bullsandcows.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(UiState(
        statusText = app.getString(R.string.status_configure)
    ))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Mutable AI object kept outside immutable state
    private var compPlayer: ComputerPlayer? = null

    // ---- String helper ----

    private fun s(@StringRes id: Int, vararg args: Any?): String =
        getApplication<Application>().getString(id, *args)

    // ---- Settings ----

    fun updateSettings(settings: GameSettings) {
        _uiState.update { it.copy(settings = settings) }
    }

    // ---- Game control ----

    fun startGame() {
        val settings = _uiState.value.settings
        compPlayer = ComputerPlayer(settings.numSize, settings.skill)

        _uiState.update {
            it.copy(
                phase = GamePhase.RUNNING,
                statusText = buildStatusText(settings),
                left = LeftPanelState(compNumber = GameNumber.generate(settings.numSize)),
                right = RightPanelState()
            )
        }

        when (settings.gameType) {
            1 -> {
                // Only player guesses
                _uiState.update { it.copy(
                    left = it.left.copy(
                        inputEnabled = true,
                        prompt = Prompt(s(R.string.prompt_enter_number, settings.numSize))
                    )
                )}
            }
            2 -> {
                // Only computer guesses
                _uiState.update { it.copy(
                    right = it.right.copy(
                        prompt = Prompt(s(R.string.prompt_think_number, settings.numSize))
                    )
                )}
                viewModelScope.launch { delay(600); doCompTurn() }
            }
            3 -> {
                // Both guess
                if (settings.whoFirst == 1) {
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            inputEnabled = true,
                            prompt = Prompt(s(R.string.prompt_enter_number, settings.numSize))
                        ),
                        right = it.right.copy(
                            prompt = Prompt(s(R.string.prompt_wait_player), PromptType.MUTED)
                        )
                    )}
                } else {
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            prompt = Prompt(s(R.string.prompt_wait_comp), PromptType.MUTED)
                        )
                    )}
                    viewModelScope.launch { delay(400); doCompTurn() }
                }
            }
        }
    }

    fun breakGame() {
        if (_uiState.value.phase != GamePhase.RUNNING) return
        val leftNumber = _uiState.value.left.compNumber
        val leftWon    = _uiState.value.left.won
        endGame()
        if (leftNumber.isNotEmpty() && !leftWon) {
            _uiState.update { it.copy(
                left = it.left.copy(
                    prompt = Prompt(s(R.string.prompt_game_broken, leftNumber), PromptType.WARN)
                )
            )}
        }
        _uiState.update { it.copy(statusText = s(R.string.status_game_broken)) }
    }

    private fun endGame() {
        _uiState.update { it.copy(
            phase = GamePhase.FINISHED,
            left  = it.left.copy(inputEnabled = false),
            right = it.right.copy(inputEnabled = false)
        )}
    }

    // ---- Left panel: player guesses the computer's number ----

    fun onLeftMove(guess: String) {
        val state   = _uiState.value
        val numSize = state.settings.numSize

        if (guess.lowercase() == "number") {
            if (state.left.attempts.size >= 4) revealCompNumber()
            else _uiState.update { it.copy(
                left = it.left.copy(
                    prompt = Prompt(s(R.string.prompt_number_too_early), PromptType.WARN)
                )
            )}
            return
        }

        if (!GameNumber.isValid(guess, numSize)) {
            _uiState.update { it.copy(
                left = it.left.copy(
                    prompt = Prompt(s(R.string.prompt_invalid_number, numSize), PromptType.ERROR)
                )
            )}
            return
        }

        processLeftMove(guess)
    }

    private fun processLeftMove(guess: String) {
        val state   = _uiState.value
        val secret  = state.left.compNumber
        val numSize = state.settings.numSize
        val bulls   = GameNumber.bulls(secret, guess)
        val cows    = GameNumber.cows(secret, guess)
        val attempt = Attempt(guess, bulls, cows)
        val newAttempts = state.left.attempts + attempt
        val won = (bulls == numSize)

        if (won) {
            val n   = newAttempts.size
            val msg = if (state.left.practicing)
                s(R.string.prompt_player_won_practice, secret, n)
            else
                s(R.string.prompt_player_won, secret, n)

            _uiState.update { it.copy(
                left = it.left.copy(
                    attempts     = newAttempts,
                    won          = true,
                    inputEnabled = false,
                    prompt       = Prompt(msg, PromptType.SUCCESS)
                )
            )}
            afterLeftWin()
        } else {
            _uiState.update { it.copy(
                left = it.left.copy(
                    attempts = newAttempts,
                    prompt   = Prompt(s(R.string.prompt_continue_need_bulls, numSize))
                )
            )}

            if (state.settings.gameType == 3) {
                if (state.right.won) {
                    // computer already won — player continues as practice
                    if (!state.left.practicing) {
                        _uiState.update { it.copy(
                            left = it.left.copy(
                                practicing   = true,
                                inputEnabled = true,
                                prompt       = Prompt(s(R.string.prompt_player_lost), PromptType.WARN)
                            )
                        )}
                    } else {
                        _uiState.update { it.copy(left = it.left.copy(inputEnabled = true)) }
                    }
                } else {
                    _uiState.update { it.copy(left = it.left.copy(inputEnabled = false)) }
                    viewModelScope.launch { delay(400); doCompTurn() }
                }
            } else {
                // game type 1: just re-enable input
                _uiState.update { it.copy(left = it.left.copy(inputEnabled = true)) }
            }
        }
    }

    private fun revealCompNumber() {
        val state   = _uiState.value
        val secret  = state.left.compNumber
        val numSize = state.settings.numSize
        val attempt = Attempt(secret, numSize, 0, isWin = false, isReveal = true)

        _uiState.update { it.copy(
            left = it.left.copy(
                won          = true,
                inputEnabled = false,
                attempts     = it.left.attempts + attempt,
                prompt       = Prompt(s(R.string.prompt_revealed, secret), PromptType.WARN)
            )
        )}
        afterLeftWin()
    }

    private fun afterLeftWin() {
        val state = _uiState.value
        when (state.settings.gameType) {
            1 -> endGame()
            3 -> when {
                state.right.won            -> endGame()
                state.settings.lastMove    -> viewModelScope.launch { delay(400); doCompTurn() }
                else                       -> endGame()
            }
        }
    }

    // ---- Right panel: computer guesses the player's number ----

    fun onRightAnswer(bulls: Int, cows: Int) {
        val numSize = _uiState.value.settings.numSize
        if (bulls < 0 || cows < 0 || bulls + cows > numSize || bulls > numSize) {
            _uiState.update { it.copy(
                right = it.right.copy(
                    prompt = Prompt(s(R.string.prompt_invalid_answer, numSize), PromptType.ERROR)
                )
            )}
            return
        }
        processRightAnswer(bulls, cows)
    }

    private fun processRightAnswer(bulls: Int, cows: Int) {
        val state   = _uiState.value
        val move    = state.right.currentMove
        val numSize = state.settings.numSize
        val won     = (bulls == numSize)

        compPlayer?.recordAnswer(move, bulls, cows)

        val attempt     = Attempt(move, bulls, cows, isWin = won)
        val newAttempts = state.right.attempts + attempt

        _uiState.update { it.copy(
            right = it.right.copy(
                attempts     = newAttempts,
                inputEnabled = false
            )
        )}

        if (won) {
            val n   = compPlayer?.size ?: newAttempts.size
            val msg = if (state.right.practicing)
                s(R.string.prompt_comp_won_practice, n)
            else
                s(R.string.prompt_comp_won, n)

            _uiState.update { it.copy(
                right = it.right.copy(
                    won    = true,
                    prompt = Prompt(msg, PromptType.SUCCESS)
                )
            )}
            afterRightWin()
        } else {
            _uiState.update { it.copy(
                right = it.right.copy(
                    prompt = Prompt(s(R.string.prompt_wait_your_answer))
                )
            )}

            if (state.settings.gameType == 3) {
                if (state.left.won) {
                    // player already won — computer continues as practice
                    if (!state.right.practicing) {
                        _uiState.update { it.copy(
                            right = it.right.copy(
                                practicing = true,
                                prompt     = Prompt(s(R.string.prompt_comp_lost), PromptType.WARN)
                            )
                        )}
                    }
                    viewModelScope.launch { delay(400); doCompTurn() }
                } else {
                    val numS = state.settings.numSize
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            inputEnabled = true,
                            prompt       = Prompt(s(R.string.prompt_enter_number, numS))
                        )
                    )}
                }
            } else {
                // game type 2: computer keeps guessing
                viewModelScope.launch { delay(400); doCompTurn() }
            }
        }
    }

    private fun afterRightWin() {
        val state = _uiState.value
        when (state.settings.gameType) {
            2 -> endGame()
            3 -> when {
                state.left.won          -> endGame()
                state.settings.lastMove -> {
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            inputEnabled = true,
                            prompt       = Prompt(s(R.string.prompt_last_chance), PromptType.WARN)
                        )
                    )}
                }
                else -> endGame()
            }
        }
    }

    // ---- Computer's turn ----

    private fun doCompTurn() {
        val move = compPlayer?.generateMove()
        if (move == null) {
            _uiState.update { it.copy(
                right = it.right.copy(
                    inputEnabled = false,
                    prompt       = Prompt(s(R.string.prompt_contradictory), PromptType.ERROR)
                )
            )}
            endGame()
            return
        }
        _uiState.update { it.copy(
            right = it.right.copy(
                currentMove  = move,
                inputEnabled = true,
                prompt       = Prompt(s(R.string.prompt_comp_move, move))
            )
        )}
    }

    // ---- Status bar text ----

    private fun buildStatusText(settings: GameSettings): String {
        val typeText = when (settings.gameType) {
            1    -> s(R.string.status_you_guess)
            2    -> s(R.string.status_comp_guesses)
            else -> s(R.string.status_both_guess)
        }
        val skillLabels = listOf(
            s(R.string.skill_highest),
            s(R.string.skill_high),
            s(R.string.skill_medium),
            s(R.string.skill_low)
        )
        val numText = s(R.string.status_digits, settings.numSize)
        var result  = "$typeText | $numText"
        if (settings.gameType != 1)
            result += " | " + s(R.string.status_skill, skillLabels[settings.skill - 1])
        if (settings.gameType == 3) {
            val firstLabels = listOf(s(R.string.first_move_you), s(R.string.first_move_comp))
            result += " | " + s(R.string.status_first, firstLabels[settings.whoFirst - 1])
        }
        return result
    }
}

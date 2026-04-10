package com.example.bullsandcows.viewmodel

import android.app.Application
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Mutable AI object kept outside immutable state
    private var compPlayer: ComputerPlayer? = null

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
                phase      = GamePhase.RUNNING,
                statusSpec = StatusSpec.Running(settings),
                left       = LeftPanelState(compNumber = GameNumber.generate(settings.numSize)),
                right      = RightPanelState()
            )
        }

        when (settings.gameType) {
            1 -> {
                _uiState.update { it.copy(
                    left = it.left.copy(
                        inputEnabled = true,
                        prompt = Prompt(R.string.prompt_enter_number, listOf(settings.numSize))
                    )
                )}
            }
            2 -> {
                _uiState.update { it.copy(
                    right = it.right.copy(
                        prompt = Prompt(R.string.prompt_think_number, listOf(settings.numSize))
                    )
                )}
                viewModelScope.launch { delay(600); doCompTurn() }
            }
            3 -> {
                if (settings.whoFirst == 1) {
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            inputEnabled = true,
                            prompt = Prompt(R.string.prompt_enter_number, listOf(settings.numSize))
                        ),
                        right = it.right.copy(
                            prompt = Prompt(R.string.prompt_wait_player, type = PromptType.MUTED)
                        )
                    )}
                } else {
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            prompt = Prompt(R.string.prompt_wait_comp, type = PromptType.MUTED)
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
                    prompt = Prompt(R.string.prompt_game_broken, listOf(leftNumber), PromptType.WARN)
                )
            )}
        }
        _uiState.update { it.copy(statusSpec = StatusSpec.Broken) }
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
                    prompt = Prompt(R.string.prompt_number_too_early, type = PromptType.WARN)
                )
            )}
            return
        }

        if (!GameNumber.isValid(guess, numSize)) {
            _uiState.update { it.copy(
                left = it.left.copy(
                    prompt = Prompt(R.string.prompt_invalid_number, listOf(numSize), PromptType.ERROR)
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
            val n      = newAttempts.size
            val resId  = if (state.left.practicing) R.string.prompt_player_won_practice
                         else R.string.prompt_player_won

            _uiState.update { it.copy(
                left = it.left.copy(
                    attempts     = newAttempts,
                    won          = true,
                    inputEnabled = false,
                    prompt       = Prompt(resId, listOf(secret, n), PromptType.SUCCESS)
                )
            )}
            afterLeftWin()
        } else {
            _uiState.update { it.copy(
                left = it.left.copy(
                    attempts = newAttempts,
                    prompt   = Prompt(R.string.prompt_continue_need_bulls, listOf(numSize))
                )
            )}

            if (state.settings.gameType == 3) {
                if (state.right.won) {
                    if (!state.left.practicing) {
                        _uiState.update { it.copy(
                            left = it.left.copy(
                                practicing   = true,
                                inputEnabled = true,
                                prompt       = Prompt(R.string.prompt_player_lost, type = PromptType.WARN)
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
                prompt       = Prompt(R.string.prompt_revealed, listOf(secret), PromptType.WARN)
            )
        )}
        afterLeftWin()
    }

    private fun afterLeftWin() {
        val state = _uiState.value
        when (state.settings.gameType) {
            1 -> endGame()
            3 -> when {
                state.right.won         -> endGame()
                state.settings.lastMove -> viewModelScope.launch { delay(400); doCompTurn() }
                else                    -> endGame()
            }
        }
    }

    // ---- Right panel: computer guesses the player's number ----

    fun onRightAnswer(bulls: Int, cows: Int) {
        val numSize = _uiState.value.settings.numSize
        if (bulls < 0 || cows < 0 || bulls + cows > numSize || bulls > numSize) {
            _uiState.update { it.copy(
                right = it.right.copy(
                    prompt = Prompt(R.string.prompt_invalid_answer, listOf(numSize), PromptType.ERROR)
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
            val n     = compPlayer?.size ?: newAttempts.size
            val resId = if (state.right.practicing) R.string.prompt_comp_won_practice
                        else R.string.prompt_comp_won

            _uiState.update { it.copy(
                right = it.right.copy(
                    won    = true,
                    prompt = Prompt(resId, listOf(n), PromptType.SUCCESS)
                )
            )}
            afterRightWin()
        } else {
            _uiState.update { it.copy(
                right = it.right.copy(
                    prompt = Prompt(R.string.prompt_wait_your_answer)
                )
            )}

            if (state.settings.gameType == 3) {
                if (state.left.won) {
                    if (!state.right.practicing) {
                        _uiState.update { it.copy(
                            right = it.right.copy(
                                practicing = true,
                                prompt     = Prompt(R.string.prompt_comp_lost, type = PromptType.WARN)
                            )
                        )}
                    }
                    viewModelScope.launch { delay(400); doCompTurn() }
                } else {
                    _uiState.update { it.copy(
                        left = it.left.copy(
                            inputEnabled = true,
                            prompt       = Prompt(R.string.prompt_enter_number, listOf(numSize))
                        )
                    )}
                }
            } else {
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
                            prompt       = Prompt(R.string.prompt_last_chance, type = PromptType.WARN)
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
                    prompt       = Prompt(R.string.prompt_contradictory, type = PromptType.ERROR)
                )
            )}
            endGame()
            return
        }
        _uiState.update { it.copy(
            right = it.right.copy(
                currentMove  = move,
                inputEnabled = true,
                prompt       = Prompt(R.string.prompt_comp_move, listOf(move))
            )
        )}
    }
}

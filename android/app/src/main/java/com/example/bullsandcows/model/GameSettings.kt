package com.example.bullsandcows.model

/**
 * Immutable snapshot of user-selected game settings.
 *
 * @param gameType  1 = computer thinks / human guesses
 *                  2 = human thinks / computer guesses
 *                  3 = both think and guess
 * @param numSize   Number of digits (3–6)
 * @param skill     AI difficulty: 1 = highest … 4 = lowest
 * @param whoFirst  Who moves first in type-3 game: 1 = player, 2 = computer
 * @param lastMove  If true, the losing side gets one extra turn after the opponent wins
 */
data class GameSettings(
    val gameType: Int = 3,
    val numSize: Int = 4,
    val skill: Int = 2,
    val whoFirst: Int = 1,
    val lastMove: Boolean = true
)

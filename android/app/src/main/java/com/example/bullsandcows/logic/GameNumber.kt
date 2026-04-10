package com.example.bullsandcows.logic

object GameNumber {

    /** Generates a valid random number for the game. */
    fun generate(size: Int): String {
        val digits = (0..9).toMutableList()
        var result: String
        do {
            digits.shuffle()
            result = digits.take(size).joinToString("")
        } while (result[0] == '0')
        return result
    }

    /** Number of bulls: correct digit in the correct position. */
    fun bulls(secret: String, guess: String): Int =
        secret.indices.count { secret[it] == guess[it] }

    /** Number of cows: correct digit in the wrong position. */
    fun cows(secret: String, guess: String): Int =
        guess.indices.count { i ->
            val idx = secret.indexOf(guess[i])
            idx >= 0 && idx != i
        }

    /** Validates: correct length, no leading zero, all digits unique. */
    fun isValid(s: String, size: Int): Boolean {
        if (s.length != size || s[0] == '0') return false
        if (s.any { !it.isDigit() }) return false
        return s.length == s.toSet().size
    }
}

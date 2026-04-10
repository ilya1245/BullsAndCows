package com.example.bullsandcows.logic

import kotlin.math.pow

data class MoveRecord(val number: String, val bulls: Int, val cows: Int)

/**
 * AI engine — exact port of CompMoves from game.js.
 *
 * Phase 1: sequential slices of a pre-shuffled 10-digit string (exploration).
 * Phase 2: constraint-satisfaction search over 9 random intervals.
 * Skill levels 1–4 selectively forget best/worst past moves.
 */
class ComputerPlayer(val numberSize: Int, val skill: Int) {

    private val moveStore = mutableListOf<MoveRecord>()
    private var reserveStore = mutableListOf<MoveRecord>()
    private var usedIntervals = ""
    private val moveString = buildMoveString()

    val size get() = moveStore.size

    // ---- Initialisation ----

    private fun buildMoveString(): String {
        val chars = ('0'..'9').toMutableList()
        var result: String
        do {
            chars.shuffle()
            result = chars.joinToString("")
        } while (hasLeadingZeroInSegment(result))
        return result
    }

    private fun hasLeadingZeroInSegment(s: String): Boolean {
        var i = 0
        while (i < 10) {
            if (s[i] == '0') return true
            i += numberSize
        }
        return false
    }

    // ---- Move generation ----

    /**
     * Returns the next computer move, or null if player answers are contradictory.
     */
    fun generateMove(): String? {
        val usedDigits = numberSize * moveStore.size

        // Phase 1: take next slice from pre-shuffled string
        if (sumBC() < numberSize && 10 - usedDigits >= numberSize) {
            return moveString.substring(usedDigits, usedDigits + numberSize)
        }

        // Phase 2: constraint-satisfaction search
        reserveStore = moveStore.toMutableList()

        if (skill == 2 && moveStore.size >= 3)      hideWorstMove()
        if (skill >= 3 && moveStore.size + 1 >= 4)  hideBestMove()
        if (skill == 4 && moveStore.size + 1 >= 5)  hideWorstMove()

        var move: String? = null
        var attempts = 0
        while (move == null && attempts < 1000) {
            attempts++
            val interval = (0..8).random()
            if (usedIntervals.contains(interval.toString())) continue

            val base = 10.0.pow(numberSize - 1).toLong()
            val from = base * (1 + interval)
            val to   = base * (2 + interval)
            val dir  = if (Math.random() < 0.5) 1L else -1L

            move = findMove(from, to, dir)
            if (move == null) {
                usedIntervals += interval.toString()
                if (usedIntervals.length == 9) {
                    moveStore.clear()
                    moveStore.addAll(reserveStore)
                    return null  // contradictory answers
                }
            }
        }

        moveStore.clear()
        moveStore.addAll(reserveStore)
        return move
    }

    private fun findMove(fromArg: Long, toArg: Long, dir: Long): String? {
        val from = if (dir < 0) toArg - 1 else fromArg
        val to   = if (dir < 0) fromArg - 1 else toArg
        var i = from
        while (i != to) {
            val candidate = i.toString()
            if (GameNumber.isValid(candidate, numberSize)) {
                val valid = moveStore.all { m ->
                    GameNumber.bulls(candidate, m.number) == m.bulls &&
                    GameNumber.cows(candidate, m.number)  == m.cows
                }
                if (valid && reserveStore.none { it.number == candidate }) {
                    return candidate
                }
            }
            i += dir
        }
        return null
    }

    private fun sumBC() = moveStore.sumOf { it.bulls + it.cows }

    // ---- Skill: selective forgetting ----

    private fun hideBestMove() {
        var bestSum = -1
        var bestIdx = 0
        moveStore.forEachIndexed { idx, m ->
            val sum = m.bulls + m.cows
            if (sum > bestSum || (sum == bestSum && m.bulls > moveStore[bestIdx].bulls)) {
                bestSum = sum; bestIdx = idx
            }
        }
        usedIntervals = ""
        moveStore.removeAt(bestIdx)
    }

    private fun hideWorstMove() {
        var worstSum = Int.MAX_VALUE
        var worstIdx = 0
        moveStore.forEachIndexed { idx, m ->
            val sum = m.bulls + m.cows
            if (sum < worstSum || (sum == worstSum && m.bulls < moveStore[worstIdx].bulls)) {
                worstSum = sum; worstIdx = idx
            }
        }
        usedIntervals = ""
        moveStore.removeAt(worstIdx)
    }

    // ---- Record answer ----

    fun recordAnswer(number: String, bulls: Int, cows: Int) {
        moveStore.add(MoveRecord(number, bulls, cows))
    }
}

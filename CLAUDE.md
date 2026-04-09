# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

"Bools and Cows" (Быки и Коровы) is a number-guessing game implemented in two independent versions:

1. **Java Swing desktop app** — `java/src/` (the current active version)
2. **Web app** — `html_css_js/` (single-page, no build step)

The `java/src_old/` directory contains the previous Java version kept for reference. **Never touch it.**

## Folder conventions

- `java/src_old/` — только для истории, не трогать никогда
- `java/src/` — активный Java-код, менять только при запросах об изменениях Java-версии
- `html_css_js/` — веб-версия, менять только при запросах об изменениях веб-версии

## Game Rules

Players guess a number with unique digits, no leading zero, 3–6 digits (default 4). Each guess gets:
- **Bulls (Быки)**: correct digit in the correct position
- **Cows (Коровы)**: correct digit in the wrong position

Win condition: as many bulls as the number length. Typing `number` after move 5 reveals the secret (counts as a loss).

## Running the Applications

### Java Swing App

JDK: `C:\Program Files\Java\jdk1.8.0_241`. Compile and run from `java/src/`:

```
javac -encoding UTF-8 -d out start\BoolsAndCows.java exception\ContradictoryAnswersException.java logic\GameNumber.java logic\ComputerPlayer.java view\IntFilter.java view\AnswerVerifier.java view\GameNumberVerifier.java view\MoveTableModel.java view\GameTable.java view\CompGuessPanel.java view\HumanGuessPanel.java view\BoolsAndCowsFrame.java view\HelpFrame.java view\AboutFrame.java

java -cp out start.BoolsAndCows
```

### Web App

Open `html_css_js/index.html` directly in a browser — no server or build step needed.

## Java Architecture

Entry point: `start/BoolsAndCows.java` — launches the Swing EDT.

**Package structure:**

- `logic/` — game logic, no UI dependencies
  - `GameNumber` — stores a number as a String; generates random valid numbers; provides `bulls()`, `cows()`, `isValid()`, `equals()`
  - `ComputerPlayer` — AI engine; holds move history and generates the next guess. Phase 1: sequential slices of a shuffled digit string (exploration). Phase 2: constraint-satisfaction search over random intervals. Skill levels 1–4 selectively forget best/worst past moves via private `hideBestMove()`/`hideWorstMove()`.
- `exception/` — `ContradictoryAnswersException` thrown by `ComputerPlayer.generateMove()` when player-provided answers are contradictory
- `view/` — all Swing UI plus moved helpers:
  - `BoolsAndCowsFrame` — main `JFrame`, owns game state fields and the menu bar
  - `CompGuessPanel` — panel where the human enters guesses (game type: computer thinks)
  - `HumanGuessPanel` — panel where the computer guesses (game type: human thinks)
  - `HelpFrame`, `AboutFrame` — dialogs (`JDialog`), receive parent `Frame` in constructor
  - `GameTable` — custom `JTable` with per-cell color/alignment
  - `MoveTableModel` — custom `AbstractTableModel` for move history
  - `GameNumberVerifier`, `AnswerVerifier`, `IntFilter` — input validation helpers

**Game types** (set in Options menu):
1. Computer thinks, human guesses — only `CompGuessPanel` shown
2. Human thinks, computer guesses — only `HumanGuessPanel` shown
3. Both think, both guess — both panels shown, alternating turns

**AI algorithm** (`ComputerPlayer`): initial moves use pre-shuffled digit string segments (Phase 1, max 2 moves for 4-digit game). Once bulls+cows sum ≥ numberSize, switches to constraint-satisfaction search over 9 random intervals (Phase 2). Contradictory input → `ContradictoryAnswersException`.

## Web Architecture

Three files, no dependencies or build:
- `index.html` — layout, settings controls, modal help dialog
- `style.css` — all styling
- `game.js` — everything in one file

`game.js` structure:
- `BCNumber` — pure utility object (`generate`, `bools`, `cows`, `isValid`)
- `CompMoves` class — exact JS port of the Java AI
- `game` — global state object (settings + per-panel state for `left`/`right`)
- Functions: `startGame`, `endGame`, `breakGame`, `doCompTurn`, panel builders, input lock/unlock helpers, table row rendering

The `left` panel is the human guessing; the `right` panel is the computer guessing. In game type 3 (both), turns alternate between the two panels.

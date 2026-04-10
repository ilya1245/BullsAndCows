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

## Android Architecture

Project location: `android/` (Kotlin + Jetpack Compose, minSdk 26).

**Package structure:**

- `logic/` — pure Kotlin, no Android dependencies
  - `GameNumber` — port of `BCNumber` from JS: `generate`, `bulls`, `cows`, `isValid`
  - `ComputerPlayer` — port of `CompMoves` from JS: Phase 1 (shuffled string slices), Phase 2 (constraint-satisfaction), skill levels 1–4 via `hideBestMove`/`hideWorstMove`
- `model/` — data classes only
  - `GameSettings` — immutable settings snapshot (gameType, numSize, skill, whoFirst, lastMove)
  - `UiState` — root state tree: `GamePhase`, `Prompt`, `Attempt`, `LeftPanelState`, `RightPanelState`
- `viewmodel/`
  - `GameViewModel` — all game logic, exposes `StateFlow<UiState>`; uses `viewModelScope` for delayed computer turns
- `ui/theme/` — `Theme.kt` with `BullsAndCowsTheme`, color constants (`BullColor`, `CowColor`, etc.)
- `ui/screens/` — `MainScreen.kt` (single screen)
- `ui/components/` — `SettingsBar` (TopBar + DrawerContent), `CompactTextField`, `GamePanel` (WelcomeArea / PlayerPanel / ComputerPanel / GameArea), `HistoryTable`, `LeftInputRow`, `RightInputRow`, `HelpDialog`

**UI layout:** Single `ModalNavigationDrawer`. `TopBar` (36dp) shows ☰ + status text inline. All settings and control buttons live in `DrawerContent` (hamburger drawer). `StatusBar` was removed.

**Input fields:** `CompactOutlinedTextField` (`CompactTextField.kt`) wraps `BasicTextField` + `OutlinedTextFieldDefaults.DecorationBox` with custom `contentPadding`. This is required because `contentPadding` is not a public param of `OutlinedTextField` in BOM 2024.02.00.

**Focus management:** `PlayerPanel` and `ComputerPanel` each hold a `FocusRequester` and use `LaunchedEffect(state.inputEnabled)` to auto-focus their input field when enabled. For this to work in game type 3, `GameViewModel.processLeftMove` must explicitly set `left.inputEnabled = false` before scheduling the computer turn.

**Build:**
```
cd android
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```
JAVA_HOME must point to Android Studio's bundled JDK 21 — system JDK 25 is incompatible with AGP 8.2.2.

**Install on LDPlayer:**
```
"/d/LDPlayer/LDPlayer9/adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
```

**Localisation:** `res/values/strings.xml` (English default) + `res/values-uk/strings.xml` (Ukrainian). Language follows system locale — no in-app switcher.

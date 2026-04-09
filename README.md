# Bulls and Cows

Bulls and Cows is a number guessing game implemented in two independent versions:

1. Java Swing desktop application (`java/src/`)
2. Web application (`html_css_js/`)

## Game Rules

- The player guesses a secret number with unique digits.
- The number cannot start with zero.
- Valid lengths are 3 to 6 digits (default is 4).
- Each guess receives feedback:
  - **Bulls**: digits that are correct and in the correct position.
  - **Cows**: digits that are correct but in the wrong position.
- The game is won when the number of bulls equals the secret number length.
- In the Java version, entering `number` after move 5 reveals the secret and counts as a loss.

## Project Structure

- `java/src/` — active Java Swing application.
- `java/src_old/` — legacy Java version kept for reference. Do not modify.
- `html_css_js/` — web application version with no build step.

## Java Swing App

### Run instructions

From the `java/src/` folder, compile and run with a Java JDK:

```bash
javac -encoding UTF-8 -d out \
  start\BoolsAndCows.java \
  exception\ContradictoryAnswersException.java \
  logic\GameNumber.java \
  logic\ComputerPlayer.java \
  view\IntFilter.java \
  view\AnswerVerifier.java \
  view\GameNumberVerifier.java \
  view\MoveTableModel.java \
  view\GameTable.java \
  view\CompGuessPanel.java \
  view\HumanGuessPanel.java \
  view\BoolsAndCowsFrame.java \
  view\HelpFrame.java \
  view\AboutFrame.java

java -cp out start.BoolsAndCows
```

## Web App

Open `html_css_js/index.html` directly in a browser. No server or build step is required.

## Notes

- The Java implementation contains a human vs computer mode, computer vs human mode, and a dual mode where both sides guess.
- The AI engine in Java is implemented in `logic/ComputerPlayer.java`.
- The web version is implemented in `html_css_js/js/game.js`.

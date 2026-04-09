# Building a JAR File

## Compile and Package

From the `java/src/` directory, run the following commands to create an executable JAR:

### Step 1: Compile
```bash
javac -encoding UTF-8 -d out ^
  start\BoolsAndCows.java ^
  exception\ContradictoryAnswersException.java ^
  logic\GameNumber.java ^
  logic\ComputerPlayer.java ^
  view\IntFilter.java ^
  view\AnswerVerifier.java ^
  view\GameNumberVerifier.java ^
  view\MoveTableModel.java ^
  view\GameTable.java ^
  view\CompGuessPanel.java ^
  view\HumanGuessPanel.java ^
  view\BoolsAndCowsFrame.java ^
  view\HelpFrame.java ^
  view\AboutFrame.java
```

### Step 2: Create JAR
```bash
jar cfe BullsAndCows.jar start.BoolsAndCows -C out .
```

This creates `BullsAndCows.jar` with `start.BoolsAndCows` as the main entry point.

## Run the JAR

```bash
java -jar BullsAndCows.jar
```

## Notes

- The application uses only standard Java Swing libraries, so no external dependencies are required.
- The `-encoding UTF-8` flag ensures proper handling of international characters.
- The `cfe` flags in jar command mean:
  - `c` — create a new archive
  - `f` — specify the archive file name
  - `e` — define the entry point (main class)

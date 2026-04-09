package logic;

import exception.ContradictoryAnswersException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ComputerPlayer {
    private static final Random RANDOM = new Random();

    private final List<MoveWithAnswer> moveStore    = new ArrayList<>();
    private final List<MoveWithAnswer> reserveStore = new ArrayList<>();
    private final int numberSize;
    private final int skill;
    private Set<Integer> usedIntervals = new HashSet<>();
    private String moveString = "1234567890";

    public ComputerPlayer(int digitQuantity, int skill) {
        this.numberSize = digitQuantity;
        this.skill      = skill;
        shuffleMoveString();
    }

    // -----------------------------------------------------------------------
    // Генерация случайной перестановки цифр, гарантируя отсутствие ведущего нуля
    // -----------------------------------------------------------------------
    private void shuffleMoveString() {
        char[] chars;
        do {
            chars = "1234567890".toCharArray();
            for (int i = 0; i < numberSize * 3; i++) {
                int a = RANDOM.nextInt(10);
                int b = RANDOM.nextInt(10);
                if (a != b) {
                    char tmp = chars[a];
                    chars[a] = chars[b];
                    chars[b] = tmp;
                }
            }
        } while (hasLeadingZeroInSegment(chars));
        moveString = new String(chars);
    }

    private boolean hasLeadingZeroInSegment(char[] chars) {
        for (int i = 0; i < 10; i += numberSize) {
            if (chars[i] == '0') return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Основной метод генерации хода компьютера
    // -----------------------------------------------------------------------
    public GameNumber generateMove() throws ContradictoryAnswersException {
        int usedDigits = numberSize * moveStore.size();
        if (sumBullsAndCows() < numberSize && 10 - usedDigits >= numberSize) {
            return new GameNumber(moveString.substring(usedDigits, usedDigits + numberSize));
        }

        reserveStore.clear();
        reserveStore.addAll(moveStore);

        if (skill == 2 && size() >= 3)       hideWorstMove();
        if (skill >= 3 && size() + 1 >= 4)   hideBestMove();
        if (skill == 4 && size() + 1 >= 5)   hideWorstMove();

        GameNumber result = null;
        while (result == null) {
            int interval = RANDOM.nextInt(9);
            if (usedIntervals.contains(interval)) continue;

            int base      = (int) Math.pow(10, numberSize - 1);
            int from      = base * (1 + interval);
            int to        = base * (2 + interval);
            int direction = RANDOM.nextBoolean() ? 1 : -1;

            result = findMove(from, to, direction);
            if (result == null) {
                usedIntervals.add(interval);
                if (usedIntervals.size() == 9) throw new ContradictoryAnswersException();
            }
        }

        moveStore.clear();
        moveStore.addAll(reserveStore);
        return result;
    }

    /** Ищет ход, совместимый со всеми предыдущими ответами. Возвращает null если не найден. */
    private GameNumber findMove(int from, int to, int direction) {
        if (direction < 0) {
            int tmp = from;
            from = to - 1;
            to   = tmp - 1;
        }
        for (int i = from; i != to; i += direction) {
            GameNumber candidate = new GameNumber(Integer.toString(i));
            if (!GameNumber.isValid(candidate, numberSize)) continue;

            boolean valid = true;
            for (MoveWithAnswer mwa : moveStore) {
                if (candidate.bulls(mwa.number) != mwa.bulls
                        || candidate.cows(mwa.number) != mwa.cows) {
                    valid = false;
                    break;
                }
            }
            if (!valid) continue;

            boolean alreadyUsed = false;
            for (MoveWithAnswer mwa : reserveStore) {
                if (candidate.equals(mwa.number)) {
                    alreadyUsed = true;
                    break;
                }
            }
            if (!alreadyUsed) return candidate;
        }
        return null;
    }

    private int sumBullsAndCows() {
        int sum = 0;
        for (MoveWithAnswer m : moveStore) sum += m.bulls + m.cows;
        return sum;
    }

    // -----------------------------------------------------------------------
    // Управление историей ходов
    // -----------------------------------------------------------------------
    public int size() {
        return moveStore.size();
    }

    public void addMove(GameNumber move, int bulls, int cows) {
        moveStore.add(new MoveWithAnswer(move, bulls, cows));
    }

    public GameNumber getMove(int i)  { return moveStore.get(i).number; }
    public int getBulls(int i)        { return moveStore.get(i).bulls; }
    public int getCows(int i)         { return moveStore.get(i).cows; }

    // -----------------------------------------------------------------------
    // Стратегии сокрытия ходов (управление сложностью)
    // -----------------------------------------------------------------------
    private void hideBestMove() {
        int bestSum = -1, bestIdx = 0;
        for (int i = 0; i < size(); i++) {
            MoveWithAnswer m = moveStore.get(i);
            int sum = m.bulls + m.cows;
            if (sum > bestSum || (sum == bestSum && m.bulls > moveStore.get(bestIdx).bulls)) {
                bestSum = sum;
                bestIdx = i;
            }
        }
        usedIntervals.clear();
        moveStore.remove(bestIdx);
    }

    private void hideWorstMove() {
        int worstSum = Integer.MAX_VALUE, worstIdx = 0;
        for (int i = 0; i < size(); i++) {
            MoveWithAnswer m = moveStore.get(i);
            int sum = m.bulls + m.cows;
            if (sum < worstSum || (sum == worstSum && m.bulls < moveStore.get(worstIdx).bulls)) {
                worstSum = sum;
                worstIdx = i;
            }
        }
        usedIntervals.clear();
        moveStore.remove(worstIdx);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        for (int i = 0; i < size(); i++) {
            sb.append(getMove(i)).append(" ").append(getBulls(i)).append(" ").append(getCows(i)).append("\n");
        }
        return sb.append("\n").toString();
    }

    // -----------------------------------------------------------------------
    // Внутренний класс: ход с ответом
    // -----------------------------------------------------------------------
    private static class MoveWithAnswer {
        final GameNumber number;
        final int bulls;
        final int cows;

        MoveWithAnswer(GameNumber number, int bulls, int cows) {
            this.number = number;
            this.bulls  = bulls;
            this.cows   = cows;
        }
    }
}

package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameNumber {
    private static final Random RANDOM = new Random();

    private final String str;

    public GameNumber(int digitQuantity) {
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i <= 9; i++) digits.add(i);

        String result;
        do {
            Collections.shuffle(digits, RANDOM);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digitQuantity; i++) {
                sb.append(digits.get(i));
            }
            result = sb.toString();
        } while (result.charAt(0) == '0');

        this.str = result;
    }

    public GameNumber(String inpString) {
        this.str = inpString;
    }

    /** Быки: цифра угадана и стоит на своём месте. */
    public int bulls(GameNumber guess) {
        String secret   = this.toString();
        String guessStr = guess.toString();
        int count = 0;
        for (int i = 0; i < secret.length(); i++) {
            if (secret.charAt(i) == guessStr.charAt(i)) count++;
        }
        return count;
    }

    /** Коровы: цифра угадана, но стоит не на своём месте. */
    public int cows(GameNumber guess) {
        String secret   = this.toString();
        String guessStr = guess.toString();
        int count = 0;
        for (int i = 0; i < guessStr.length(); i++) {
            int idx = secret.indexOf(guessStr.charAt(i));
            if (idx >= 0 && idx != i) count++;
        }
        return count;
    }

    public static boolean isValid(GameNumber number, int numberSize) {
        String s = number.toString();
        if (s.length() != numberSize || s.charAt(0) == '0') return false;
        for (int i = 0; i < numberSize; i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
            for (int j = 0; j < i; j++) {
                if (s.charAt(i) == s.charAt(j)) return false;
            }
        }
        return true;
    }

    public int size() {
        return toString().length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameNumber)) return false;
        return this.toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return str;
    }
}

package view;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import logic.GameNumber;

public class GameNumberVerifier extends InputVerifier {
    private final int numSize;

    public GameNumberVerifier(int numSize) {
        this.numSize = numSize;
    }

    @Override
    public boolean verify(JComponent component) {
        JFormattedTextField field = (JFormattedTextField) component;
        return GameNumber.isValid(new GameNumber(field.getText()), numSize);
    }
}

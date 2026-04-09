package view;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

public class AnswerVerifier extends InputVerifier {

    @Override
    public boolean verify(JComponent component) {
        JFormattedTextField field = (JFormattedTextField) component;
        return !field.getText().isEmpty();
    }
}

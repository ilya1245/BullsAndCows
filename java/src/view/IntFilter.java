package view;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class IntFilter extends DocumentFilter {
    private final String permit;

    public IntFilter(String permit) {
        this.permit = permit;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        super.insertString(fb, offset, filter(string), attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
            throws BadLocationException {
        super.replace(fb, offset, length, filter(string), attr);
    }

    private String filter(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(input);
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (permit.indexOf(sb.charAt(i)) < 0) sb.deleteCharAt(i);
        }
        return sb.toString();
    }
}

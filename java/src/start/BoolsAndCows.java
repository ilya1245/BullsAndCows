package start;

import javax.swing.SwingUtilities;
import view.BoolsAndCowsFrame;

public class BoolsAndCows {
    public static void main(String[] args) {
        // Запуск в потоке обработки событий Swing (EDT)
        SwingUtilities.invokeLater(() -> new BoolsAndCowsFrame().setVisible(true));
    }
}

package view;

import java.awt.Component;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class AboutFrame extends JDialog {
    private static final String ABOUT =
            "  Игра \"Быки - Коровы\"\n" +
            "  Версия: 1.4\n" +
            "  Автор: Мифлиг Илья\n" +
            "  e-mail: pilot_il2@mail.ru\n\n" +
            "  Справедливая критика:\n" +
            "     Третьяк Андрей\n" +
            "     Шевченко Александр";

    AboutFrame(Frame owner) {
        super(owner, "О программе", false);

        JTextArea textArea = new JTextArea(ABOUT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        JButton btnOk = new JButton("Ok");
        btnOk.addActionListener(e -> setVisible(false));

        JPanel keyPanel = new JPanel();
        keyPanel.add(btnOk);

        setSize(180, 200);
        setLocationRelativeTo(owner);

        getContentPane().add((Component) new JScrollPane(textArea), "Center");
        getContentPane().add((Component) keyPanel, "South");
    }
}

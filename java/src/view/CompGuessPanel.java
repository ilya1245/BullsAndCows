package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.DefaultFormatter;
import logic.GameNumber;

class CompGuessPanel extends JPanel {
    private static final String PERMITTED_CHARS = "0123456789*number";
    private static final String MSG_START_GAME  = "Начните игру в меню \"Файл\"";

    private String msgInput;
    private String msgError;
    private int    numberSize;
    private int    type;

    private GameNumber       compNumber;
    private List<GameNumber> attempts;
    boolean panelIsLocked;
    boolean focusIsLocked;

    private final GameTable           tableResult    = new GameTable(new MoveTableModel());
    private final JLabel              labelPrompt    = new JLabel();
    private final JButton             buttonMakeMove = new JButton("Сделать ход");
    private final JScrollPane         scrollPane;
    private final Box                 box;

    private final JFormattedTextField fieldAttempt = new JFormattedTextField(new DefaultFormatter() {
        private final IntFilter filter = new IntFilter(PERMITTED_CHARS);
        @Override protected IntFilter getDocumentFilter() { return filter; }
    });

    CompGuessPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        scrollPane = new JScrollPane(tableResult);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        topPanel.add((Component) new JLabel("Поле игрока", JLabel.CENTER), "North");
        topPanel.add((Component) scrollPane, "Center");
        tableResult.setFocusable(false);

        labelPrompt.setFont(new Font("Courier", Font.BOLD, 11));
        fieldAttempt.setPreferredSize(new Dimension(50, 21));

        JPanel bottomPanel1 = new JPanel();
        bottomPanel1.add(Box.createVerticalStrut(15));
        bottomPanel1.add(labelPrompt);
        bottomPanel1.setBorder(BorderFactory.createLoweredBevelBorder());

        JPanel bottomPanel21 = new JPanel();
        bottomPanel21.add(fieldAttempt);
        bottomPanel21.add(Box.createHorizontalStrut(5));
        bottomPanel21.add(buttonMakeMove);

        JPanel bottomPanel2 = new JPanel(new BorderLayout());
        bottomPanel2.add((Component) new JLabel("Ваш ход:", JLabel.CENTER), "North");
        bottomPanel2.add((Component) bottomPanel21, "Center");
        bottomPanel2.setBorder(BorderFactory.createLoweredBevelBorder());

        box = Box.createVerticalBox();
        box.add(topPanel);
        box.add(Box.createVerticalStrut(5));
        box.add(bottomPanel1);
        box.add(Box.createVerticalStrut(5));
        box.add(bottomPanel2);

        setLayout(new BorderLayout());
        add((Component) box, "Center");
        reset();
    }

    private void clearTable() {
        tableResult.setModel(new MoveTableModel());
        attempts = new ArrayList<>();
    }

    void reset() {
        lockInput();
        clearTable();
        labelPrompt.setText(MSG_START_GAME);
    }

    void waitForOpponent() {
        lockInput();
        labelPrompt.setText("");
    }

    void onGameBreak() {
        lockInput();
        if (!panelIsLocked) labelPrompt.setText(MSG_START_GAME);
    }

    void onWin(String message) {
        if (type != 3) lockInput();
        labelPrompt.setText(message);
        tableResult.setRowBackColor(attempts.size() - 1, new Color(255, 220, 220));
        panelIsLocked = true;
    }

    void start(GameNumber number, int gameType) {
        clearTable();
        compNumber = number;
        numberSize = compNumber.size();
        type       = gameType;
        panelIsLocked = false;
        msgInput = "Введите " + numberSize + "-значное число";
        msgError = "Ввод неверен. Введите " + numberSize + "-значное число";
        newTurn();
    }

    void newTurn() {
        unlockInput();
        labelPrompt.setText(msgInput);
        if (type != 3) fieldAttempt.setText("");
    }

    void lockInput() {
        fieldAttempt.setText("");
        fieldAttempt.setEnabled(false);
        buttonMakeMove.setEnabled(false);
        if (!panelIsLocked) labelPrompt.setText("");
    }

    void unlockInput() {
        if (!panelIsLocked) {
            fieldAttempt.setEnabled(true);
            buttonMakeMove.setEnabled(true);
            fieldAttempt.requestFocus();
        }
    }

    void setMoveListener(Runnable handler) {
        buttonMakeMove.addActionListener(e -> handler.run());
    }

    void addListeners() {
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                transferFocus();
                fieldAttempt.requestFocus();
            }
        });

        fieldAttempt.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) buttonMakeMove.doClick();
            }
        });

        buttonMakeMove.addActionListener(e -> {
            focusIsLocked = false;
            String text = fieldAttempt.getText();

            // Команда "number" — посмотреть задуманное число (не раньше 5-го хода)
            if (attempts.size() >= 4 && text.equals("number")) {
                attempts.add(compNumber);
                refreshTable();
                requestFocus();
                onWin("Вы подсмотрели число на " + attempts.size() + " попытке");
                return;
            }

            if (new GameNumberVerifier(numberSize).verify(fieldAttempt)) {
                GameNumber guess = new GameNumber(text);
                attempts.add(guess);
                refreshTable();
                if (compNumber.equals(guess)) {
                    requestFocus();
                    int n = attempts.size();
                    onWin("Вы разгадали число за " + n + (n > 4 ? " попыток!" : " попытки!"));
                } else if (type != 3) {
                    newTurn();
                }
            } else {
                labelPrompt.setText(msgError);
                focusIsLocked = true;
            }
        });
    }

    void refreshTable() {
        tableResult.setModel(new MoveTableModel(compNumber, attempts));
        tableResult.setColumnAlignment(0, JLabel.LEFT);
        tableResult.setColumnAlignmentBackForeColor(1, JLabel.LEFT, null, Color.RED);
        tableResult.setColumnAlignmentBackForeColor(2, JLabel.LEFT, null, Color.GREEN.darker().darker());
    }
}

package view;

import exception.ContradictoryAnswersException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.DefaultFormatter;
import logic.ComputerPlayer;
import logic.GameNumber;

class HumanGuessPanel extends JPanel {
    private static final String PERMITTED_CHARS = "0123456789";
    private static final String MSG_START_GAME  = "Начните игру в меню \"Файл\"";

    private int numberSize;
    private int type;

    boolean panelIsLocked;
    boolean focusIsLocked;

    private GameNumber     currentMove;
    private ComputerPlayer computerPlayer;

    private final GameTable           tableResult    = new GameTable(new MoveTableModel());
    private final JLabel              labelPrompt    = new JLabel();
    private final JButton             buttonMakeMove = new JButton("Дать ответ");
    private final JScrollPane         scrollPane;
    private final Box                 box;

    private final JFormattedTextField fieldBulls = new JFormattedTextField(new DefaultFormatter() {
        private final IntFilter filter = new IntFilter(PERMITTED_CHARS);
        @Override protected IntFilter getDocumentFilter() { return filter; }
    });
    private final JFormattedTextField fieldCows = new JFormattedTextField(new DefaultFormatter() {
        private final IntFilter filter = new IntFilter(PERMITTED_CHARS);
        @Override protected IntFilter getDocumentFilter() { return filter; }
    });

    HumanGuessPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        scrollPane = new JScrollPane(tableResult);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        topPanel.add((Component) new JLabel("Поле компьютера", JLabel.CENTER), "North");
        topPanel.add((Component) scrollPane, "Center");
        tableResult.setFocusable(false);

        labelPrompt.setFont(new Font("Courier", Font.BOLD, 11));
        fieldBulls.setPreferredSize(new Dimension(15, 21));
        fieldCows.setPreferredSize(new Dimension(15, 21));

        JPanel bottomPanel1 = new JPanel();
        bottomPanel1.add(Box.createVerticalStrut(15));
        bottomPanel1.add(labelPrompt);
        bottomPanel1.setBorder(BorderFactory.createLoweredBevelBorder());

        JPanel bottomPanel21 = new JPanel();
        bottomPanel21.add(new JLabel("Быки"));
        bottomPanel21.add(fieldBulls);
        bottomPanel21.add(new JLabel(" Коровы"));
        bottomPanel21.add(fieldCows);
        bottomPanel21.add(Box.createHorizontalStrut(5));
        bottomPanel21.add(buttonMakeMove);

        JPanel bottomPanel2 = new JPanel(new BorderLayout());
        bottomPanel2.add((Component) new JLabel("Ваш ответ:", JLabel.CENTER), "North");
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

    void reset() {
        lockInput();
        tableResult.setModel(new MoveTableModel());
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
        tableResult.setRowBackColor(computerPlayer.size() - 1, new Color(255, 220, 220));
        panelIsLocked = true;
    }

    void lockInput() {
        fieldBulls.setText("");
        fieldCows.setText("");
        fieldBulls.setEnabled(false);
        fieldCows.setEnabled(false);
        buttonMakeMove.setEnabled(false);
        if (!panelIsLocked) labelPrompt.setText("");
    }

    void unlockInput() {
        if (!panelIsLocked) {
            fieldBulls.setEnabled(true);
            fieldCows.setEnabled(true);
            buttonMakeMove.setEnabled(true);
        }
    }

    void start(int numSize, int gameType, int skill) {
        tableResult.setModel(new MoveTableModel());
        numberSize     = numSize;
        type           = gameType;
        panelIsLocked  = false;
        computerPlayer = new ComputerPlayer(numberSize, skill);
        newTurn();
    }

    void newTurn() {
        try {
            unlockInput();
            currentMove = computerPlayer.generateMove();
            labelPrompt.setText("Мой ход: " + currentMove);
            fieldBulls.requestFocus();
            if (type != 3) {
                fieldBulls.setText("");
                fieldCows.setText("");
            }
        } catch (ContradictoryAnswersException ex) {
            panelIsLocked = true;
            requestFocus();
            lockInput();
            labelPrompt.setText("Вы ввели ошибочные данные!!!");
        }
    }

    void setMoveListener(Runnable handler) {
        buttonMakeMove.addActionListener(e -> handler.run());
    }

    void addListeners() {
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                fieldBulls.requestFocus();
            }
        });

        AnswerVerifier verifier = new AnswerVerifier();

        fieldBulls.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (Character.isDigit(e.getKeyChar())) fieldBulls.setText("");
                if (e.getKeyCode() == KeyEvent.VK_ENTER && verifier.verify(fieldBulls)) {
                    if (verifier.verify(fieldCows)) buttonMakeMove.doClick();
                    else fieldCows.requestFocus();
                }
            }
        });

        fieldCows.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (Character.isDigit(e.getKeyChar())) fieldCows.setText("");
                if (e.getKeyCode() == KeyEvent.VK_ENTER && verifier.verify(fieldCows)) {
                    if (verifier.verify(fieldBulls)) buttonMakeMove.doClick();
                    else fieldBulls.requestFocus();
                }
            }
        });

        buttonMakeMove.addActionListener(e -> {
            focusIsLocked = false;
            if (verifier.verify(fieldBulls) && verifier.verify(fieldCows)) {
                int bulls = Integer.parseInt(fieldBulls.getText());
                int cows  = Integer.parseInt(fieldCows.getText());
                computerPlayer.addMove(currentMove, bulls, cows);
                refreshTable();
                if (bulls == numberSize) {
                    requestFocus();
                    int n = computerPlayer.size();
                    onWin(" Я разгадал число за " + n + (n > 4 ? " попыток!" : " попытки!"));
                } else if (type != 3) {
                    newTurn();
                }
            } else {
                focusIsLocked = true;
            }
        });
    }

    void refreshTable() {
        tableResult.setModel(new MoveTableModel(computerPlayer));
        tableResult.setColumnAlignment(0, JLabel.LEFT);
        tableResult.setColumnAlignmentBackForeColor(1, JLabel.LEFT, null, Color.RED);
        tableResult.setColumnAlignmentBackForeColor(2, JLabel.LEFT, null, Color.GREEN.darker().darker());
    }
}

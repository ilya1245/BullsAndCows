package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import logic.GameNumber;

public class BoolsAndCowsFrame extends JFrame {
    // Текущие настройки игры
    private int     numberSize;
    private int     gameType;
    private int     whoMovesFirst;
    private int     skill;
    private boolean lastMove;

    // Строки для строки статуса
    private String statusType  = "";
    private String statusSize  = "";
    private String statusSkill = "";
    private String statusMove  = "";

    // Меню
    private final JMenu menuFile    = new JMenu("Файл");
    private final JMenu menuOptions = new JMenu("Опции");
    private final JMenu menuHelp    = new JMenu("Помощь");

    private final JMenu menuGameType  = new JMenu("Тип игры");
    private final JMenu menuFirstMove = new JMenu("Первый ход");
    private final JMenu menuNumSize   = new JMenu("Количество цифр");
    private final JMenu menuSkill     = new JMenu("Мастерство компьютера");

    private final JMenuItem itemNewGame   = new JMenuItem("Новая игра");
    private final JMenuItem itemBreakGame = new JMenuItem("Закончить игру");
    private final JMenuItem itemExit      = new JMenuItem("Выход");
    private final JMenuItem itemHelpItem  = new JMenuItem("Правила игры");
    private final JMenuItem itemAbout     = new JMenuItem("О программе");

    private final JRadioButtonMenuItem itemSize3 = new JRadioButtonMenuItem("3");
    private final JRadioButtonMenuItem itemSize4 = new JRadioButtonMenuItem("4");
    private final JRadioButtonMenuItem itemSize5 = new JRadioButtonMenuItem("5");
    private final JRadioButtonMenuItem itemSize6 = new JRadioButtonMenuItem("6");

    private final JRadioButtonMenuItem itemComp  = new JRadioButtonMenuItem("Компьютер задумывает - Вы отгадываете");
    private final JRadioButtonMenuItem itemHuman = new JRadioButtonMenuItem("Вы задумываете - Компьютер отгадывает");
    private final JRadioButtonMenuItem itemBoth  = new JRadioButtonMenuItem("Оба задумывают - оба отгадывают");

    private final JRadioButtonMenuItem itemHumanMove = new JRadioButtonMenuItem("Вы");
    private final JRadioButtonMenuItem itemCompMove  = new JRadioButtonMenuItem("Компьютер");

    private final JRadioButtonMenuItem itemSkill1 = new JRadioButtonMenuItem("Самое высокое");
    private final JRadioButtonMenuItem itemSkill2 = new JRadioButtonMenuItem("Высокое");
    private final JRadioButtonMenuItem itemSkill3 = new JRadioButtonMenuItem("Среднее");
    private final JRadioButtonMenuItem itemSkill4 = new JRadioButtonMenuItem("Низкое");

    private final JCheckBoxMenuItem itemLastMove =
            new JCheckBoxMenuItem("Продолжать игру после победы одной из сторон");

    // Панели
    private final JPanel    contentPane = (JPanel) getContentPane();
    private final JMenuBar  menuBar     = new JMenuBar();
    private       JPanel    main        = new JPanel();
    private final JTextArea textStatus  = new JTextArea();

    // Игровые панели
    final CompGuessPanel  left  = new CompGuessPanel();
    final HumanGuessPanel right = new HumanGuessPanel();

    public BoolsAndCowsFrame() {
        buildMenu();
        buildLayout();
        bindListeners();
    }

    // -----------------------------------------------------------------------
    // Построение меню
    // -----------------------------------------------------------------------
    private void buildMenu() {
        menuFile.add(itemNewGame);
        menuFile.add(itemBreakGame);
        menuFile.add(itemExit);

        menuGameType.add(itemComp);
        menuGameType.add(itemHuman);
        menuGameType.add(itemBoth);
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(itemComp); typeGroup.add(itemHuman); typeGroup.add(itemBoth);

        menuFirstMove.add(itemHumanMove);
        menuFirstMove.add(itemCompMove);
        ButtonGroup firstGroup = new ButtonGroup();
        firstGroup.add(itemHumanMove); firstGroup.add(itemCompMove);

        menuNumSize.add(itemSize3); menuNumSize.add(itemSize4);
        menuNumSize.add(itemSize5); menuNumSize.add(itemSize6);
        ButtonGroup sizeGroup = new ButtonGroup();
        sizeGroup.add(itemSize3); sizeGroup.add(itemSize4);
        sizeGroup.add(itemSize5); sizeGroup.add(itemSize6);

        menuSkill.add(itemSkill1); menuSkill.add(itemSkill2);
        menuSkill.add(itemSkill3); menuSkill.add(itemSkill4);
        ButtonGroup skillGroup = new ButtonGroup();
        skillGroup.add(itemSkill1); skillGroup.add(itemSkill2);
        skillGroup.add(itemSkill3); skillGroup.add(itemSkill4);

        menuOptions.add(menuGameType);
        menuOptions.add(menuFirstMove);
        menuOptions.add(menuNumSize);
        menuOptions.add(menuSkill);
        menuOptions.addSeparator();
        menuOptions.add(itemLastMove);

        menuHelp.add(itemHelpItem);
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuOptions);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        itemBreakGame.setEnabled(false);
    }

    // -----------------------------------------------------------------------
    // Построение разметки
    // -----------------------------------------------------------------------
    private void buildLayout() {
        setTitle("Быки-Коровы");
        Color bgColor = new JPanel().getBackground();
        textStatus.setFont(new Font("Courier", Font.PLAIN, 10));
        textStatus.setBackground(bgColor);

        JPanel statusPanel = new JPanel();
        statusPanel.add(textStatus);

        contentPane.add(Box.createHorizontalStrut(20), "East");
        contentPane.add(Box.createHorizontalStrut(20), "West");
        contentPane.add(Box.createVerticalStrut(5),    "North");
        contentPane.add((Component) main,              "Center");
        contentPane.add((Component) statusPanel,       "South");
    }

    // -----------------------------------------------------------------------
    // Привязка обработчиков
    // -----------------------------------------------------------------------
    private void bindListeners() {
        // --- Файл ---
        itemNewGame.addActionListener(e -> startGame());
        itemBreakGame.addActionListener(e -> breakGame());
        itemExit.addActionListener(e -> { dispose(); System.exit(0); });

        // --- Тип игры ---
        itemComp.addActionListener(e -> {
            gameType = 1;
            menuFirstMove.setEnabled(false);
            itemLastMove.setEnabled(false);
            menuSkill.setEnabled(false);
            switchMainPanel(new JPanel(new BorderLayout()), () -> main.add((Component) left, "Center"));
            setSizeAndLocation(new Dimension(300, 450));
            changeStatus();
            refreshWindow();
        });
        itemHuman.addActionListener(e -> {
            gameType = 2;
            menuFirstMove.setEnabled(false);
            itemLastMove.setEnabled(false);
            menuSkill.setEnabled(true);
            switchMainPanel(new JPanel(new BorderLayout()), () -> main.add((Component) right, "Center"));
            setSizeAndLocation(new Dimension(300, 450));
            changeStatus();
            refreshWindow();
        });
        itemBoth.addActionListener(e -> {
            gameType = 3;
            menuFirstMove.setEnabled(true);
            itemLastMove.setEnabled(true);
            menuSkill.setEnabled(true);
            switchMainPanel(new JPanel(new GridLayout(1, 2, 20, 0)), () -> {
                main.add(left);
                main.add(right);
            });
            setSizeAndLocation(new Dimension(600, 450));
            changeStatus();
            refreshWindow();
        });

        // --- Первый ход ---
        itemHumanMove.addActionListener(e -> { whoMovesFirst = 1; changeStatus(); });
        itemCompMove.addActionListener(e  -> { whoMovesFirst = 2; changeStatus(); });

        // --- Размер числа ---
        itemSize3.addActionListener(e -> setNumberSize(3));
        itemSize4.addActionListener(e -> setNumberSize(4));
        itemSize5.addActionListener(e -> setNumberSize(5));
        itemSize6.addActionListener(e -> setNumberSize(6));

        // --- Уровень сложности ---
        itemSkill1.addActionListener(e -> { skill = 1; changeStatus(); });
        itemSkill2.addActionListener(e -> { skill = 2; changeStatus(); });
        itemSkill3.addActionListener(e -> { skill = 3; changeStatus(); });
        itemSkill4.addActionListener(e -> { skill = 4; changeStatus(); });

        // --- Помощь ---
        itemHelpItem.addActionListener(e -> new HelpFrame(this).setVisible(true));
        itemAbout.addActionListener(e    -> new AboutFrame(this).setVisible(true));

        // --- Закрытие окна ---
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                dispose(); System.exit(0);
            }
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                // Установка параметров по умолчанию
                itemBoth.doClick();
                itemHumanMove.doClick();
                itemSize4.doClick();
                itemLastMove.doClick();
                itemSkill2.doClick();
            }
        });

        // --- Кнопки игровых панелей ---
        left.setMoveListener(this::onLeftButtonPressed);
        right.setMoveListener(this::onRightButtonPressed);

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { main.requestFocus(); }
        });

        left.addListeners();
        right.addListeners();
    }

    // -----------------------------------------------------------------------
    // Логика кнопок в режиме "Оба" (type == 3)
    // -----------------------------------------------------------------------
    private void onLeftButtonPressed() {
        if (gameType == 3 && !left.focusIsLocked) {
            if (!left.panelIsLocked && !right.panelIsLocked) {
                right.newTurn();
                right.requestFocus();
                left.waitForOpponent();
            }
            if (left.panelIsLocked && right.panelIsLocked) { breakGame(); return; }
            if (left.panelIsLocked) {
                if (lastMove) { right.newTurn(); right.requestFocus(); left.lockInput(); }
                else          { breakGame(); return; }
            }
            if (right.panelIsLocked) {
                left.requestFocus(); left.lockInput(); left.newTurn(); right.lockInput();
            }
        } else if (left.panelIsLocked) {
            breakGame();
        }
    }

    private void onRightButtonPressed() {
        if (gameType == 3 && !right.focusIsLocked) {
            if (!right.panelIsLocked && !left.panelIsLocked) {
                left.newTurn(); left.requestFocus(); right.waitForOpponent();
            }
            if (right.panelIsLocked && left.panelIsLocked) { breakGame(); return; }
            if (right.panelIsLocked) {
                if (lastMove) { left.newTurn(); left.requestFocus(); right.lockInput(); }
                else          { breakGame(); return; }
            }
            if (left.panelIsLocked) {
                right.requestFocus(); right.lockInput(); right.newTurn(); left.lockInput();
            }
        } else if (right.panelIsLocked) {
            breakGame();
        }
    }

    // -----------------------------------------------------------------------
    // Управление игрой
    // -----------------------------------------------------------------------
    private void startGame() {
        lastMove = itemLastMove.getState();
        menuOptions.setEnabled(false);
        itemNewGame.setEnabled(false);
        itemBreakGame.setEnabled(true);

        switch (gameType) {
            case 1: left.start(new GameNumber(numberSize), gameType); break;
            case 2: right.start(numberSize, gameType, skill);         break;
            case 3: bothStart();                                       break;
        }
    }

    private void breakGame() {
        menuOptions.setEnabled(true);
        itemNewGame.setEnabled(true);
        itemBreakGame.setEnabled(false);
        requestFocus();
        left.onGameBreak();
        right.onGameBreak();
    }

    private void bothStart() {
        if (whoMovesFirst == 1) {
            left.requestFocus();
            left.start(new GameNumber(numberSize), gameType);
            right.start(numberSize, gameType, skill);
            right.waitForOpponent();
        } else {
            right.requestFocus();
            right.start(numberSize, gameType, skill);
            left.start(new GameNumber(numberSize), gameType);
            left.waitForOpponent();
        }
    }

    // -----------------------------------------------------------------------
    // Вспомогательные методы
    // -----------------------------------------------------------------------
    private void setNumberSize(int size) {
        numberSize = size;
        statusSize = size + (size <= 4 ? " цифры" : " цифр");
        changeStatus();
    }

    private void switchMainPanel(JPanel newPanel, Runnable addChildren) {
        contentPane.remove(main);
        main = newPanel;
        addChildren.run();
        contentPane.add((Component) main, "Center");
    }

    private void setSizeAndLocation(Dimension size) {
        setSize(size);
        setLocationRelativeTo(null);
    }

    private void refreshWindow() {
        contentPane.revalidate();
        contentPane.repaint();
    }

    private void changeStatus() {
        switch (gameType) {
            case 1: statusType = "Вы отгадываете "; break;
            case 2: statusType = "Комп отгадывает "; break;
            case 3: statusType = "Оба отгадывают "; break;
            default: statusType = "";
        }
        switch (skill) {
            case 1: statusSkill = ", мастерство самое высокое"; break;
            case 2: statusSkill = ", мастерство высокое";       break;
            case 3: statusSkill = ", мастерство среднее";       break;
            case 4: statusSkill = ", мастерство низкое";        break;
            default: statusSkill = "";
        }
        if (gameType == 1) statusSkill = "";

        if (gameType == 3) {
            statusMove = (whoMovesFirst == 1) ? ", первый ход - Вы" : ", первый ход - комп";
        } else {
            statusMove = "";
        }

        textStatus.setText("Статус: " + statusType + statusSize + statusSkill + statusMove);
    }
}

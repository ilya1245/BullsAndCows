package view;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import logic.ComputerPlayer;
import logic.GameNumber;

public class MoveTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = { "Ход", "Быки", "Коровы" };

    private final Object[][] data;
    private final int rowCount;

    public MoveTableModel() {
        this.data     = new Object[0][0];
        this.rowCount = 0;
    }

    /** Модель для панели игрока: компьютер задумал число, игрок угадывает. */
    public MoveTableModel(GameNumber compNumber, List<GameNumber> attempts) {
        this.rowCount = attempts.size();
        this.data     = new Object[rowCount][3];
        for (int i = 0; i < rowCount; i++) {
            GameNumber attempt = attempts.get(i);
            data[i][0] = attempt;
            data[i][1] = compNumber.bulls(attempt);
            data[i][2] = compNumber.cows(attempt);
        }
    }

    /** Модель для панели компьютера: компьютер угадывает число игрока. */
    public MoveTableModel(ComputerPlayer player) {
        this.rowCount = player.size();
        this.data     = new Object[rowCount][3];
        for (int i = 0; i < rowCount; i++) {
            data[i][0] = player.getMove(i);
            data[i][1] = player.getBulls(i);
            data[i][2] = player.getCows(i);
        }
    }

    @Override public int getColumnCount()                    { return 3; }
    @Override public int getRowCount()                       { return rowCount; }
    @Override public Object getValueAt(int row, int col)     { return data[row][col]; }
    @Override public String getColumnName(int col)           { return COLUMN_NAMES[col]; }
}

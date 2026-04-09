package view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class GameTable extends JTable {
    // Атрибуты ячеек: cellAttributes[col] — список атрибутов по строкам
    private List<CellAttr>[] cellAttributes;

    public GameTable(MoveTableModel model) {
        super(model);
    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
        int rows = model.getRowCount();
        int cols = model.getColumnCount();

        // Инициализация при первом вызове или при изменении размеров
        if (cellAttributes == null || cellAttributes.length != cols
                || cellAttributes[0].size() != rows) {
            cellAttributes = new List[cols];
            for (int i = 0; i < cols; i++) {
                cellAttributes[i] = new ArrayList<>();
            }
        }
        while (cellAttributes[0].size() < rows) {
            for (int i = 0; i < cols; i++) {
                cellAttributes[i].add(new CellAttr());
            }
        }
        paintTable();
    }

    // -----------------------------------------------------------------------
    // Установка атрибутов отдельной ячейки
    // -----------------------------------------------------------------------
    public void setCellAlignmentBackForeColor(int row, int col, Integer alignment,
            Color backColor, Color foreColor) {
        CellAttr attr = getCellAttr(row, col);
        if (alignment != null) attr.alignment = alignment;
        if (backColor  != null) attr.backColor  = backColor;
        if (foreColor  != null) attr.foreColor  = foreColor;
    }

    public void setCellAlignment(int row, int col, Integer alignment) {
        getCellAttr(row, col).alignment = alignment;
    }

    public void setCellBackColor(int row, int col, Color color) {
        getCellAttr(row, col).backColor = color;
    }

    public void setCellForeColor(int row, int col, Color color) {
        getCellAttr(row, col).foreColor = color;
    }

    // -----------------------------------------------------------------------
    // Установка атрибутов целого столбца
    // -----------------------------------------------------------------------
    public void setColumnAlignmentBackForeColor(int col, Integer alignment,
            Color backColor, Color foreColor) {
        for (int i = 0; i < getRowCount(); i++) {
            CellAttr attr = getCellAttr(i, col);
            if (alignment != null) attr.alignment = alignment;
            if (backColor  != null) attr.backColor  = backColor;
            if (foreColor  != null) attr.foreColor  = foreColor;
        }
    }

    public void setColumnAlignment(int col, Integer alignment) {
        for (int i = 0; i < getRowCount(); i++) getCellAttr(i, col).alignment = alignment;
    }

    // -----------------------------------------------------------------------
    // Установка атрибутов целой строки
    // -----------------------------------------------------------------------
    public void setRowBackColor(int row, Color color) {
        for (int i = 0; i < getColumnCount(); i++) setCellBackColor(row, i, color);
    }

    public void setRowForeColor(int row, Color color) {
        for (int i = 0; i < getColumnCount(); i++) setCellForeColor(row, i, color);
    }

    // -----------------------------------------------------------------------
    // Отрисовка
    // -----------------------------------------------------------------------
    private void paintTable() {
        for (int c = 0; c < getColumnCount(); c++) {
            paintColumn(c);
        }
    }

    private void paintColumn(int c) {
        TableColumn col = getColumn(getColumnName(c));
        col.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                int[] pos = findPosition(value);
                if (pos != null) {
                    CellAttr attr = getCellAttr(pos[0], pos[1]);
                    setHorizontalAlignment(attr.alignment);
                    setForeground(attr.foreColor);
                    setBackground(attr.backColor);
                }
                setText(value == null ? "" : value.toString());
            }
        });
    }

    private CellAttr getCellAttr(int row, int col) {
        return cellAttributes[col].get(row);
    }

    /** Находит позицию объекта в таблице по ссылке. */
    private int[] findPosition(Object value) {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                if (value == getValueAt(i, j)) return new int[]{ i, j };
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Атрибуты ячейки
    // -----------------------------------------------------------------------
    private static class CellAttr {
        int   alignment = DefaultTableCellRenderer.CENTER;
        Color backColor = Color.WHITE;
        Color foreColor = Color.BLACK;
    }
}

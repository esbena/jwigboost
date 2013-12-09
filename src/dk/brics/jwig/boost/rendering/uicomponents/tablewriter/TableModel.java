package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

import java.util.ArrayList;

/**
 * @author schwarz
 * @created 2008-10-16 20:03:38
 */
public interface TableModel {
    public Object getValueAt(int rowIndex, int columnIndex);
    public int getRowCount();
    public void addRow(Object... data);

    void addRow(ArrayList<?> data);
}

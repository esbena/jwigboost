package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

import java.util.ArrayList;

/**
 * @author schwarz
 * @created 2008-10-17 11:33:42
 */
public class DefaultTableModel implements TableModel {
    private final ArrayList<Object[]> data = new ArrayList<>();

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object[] objects = data.get(rowIndex);
        if (columnIndex >= objects.length) {
            return null;
        }
        return objects[columnIndex];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public void addRow(Object... data) {
        this.data.add(data);
    }

    @Override
    public void addRow(ArrayList<?> data) {
        this.data.add(data.toArray());
    }

}

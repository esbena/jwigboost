package dk.brics.jwig.boost.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.visualization.datasource.datatable.DataTable;

/**
 * Constructs a datatable i a more fail safe way than that of the pure
 * {@link DataTable} approach.
 * 
 * This class ensures that all datapoints are defined or null. Ensures correct
 * types in columns. Does not allow accidental overriding of datapoints. Ensures
 * correct ordering between the columns and rows.
 * 
 * @param <R>
 *            as the type of the rows
 * @see GoogleDataTableMaker
 */
public class ImprovedDataTable<R> {
    /**
     * Representation of a column
     * 
     * @param <T>
     *            as the type of all the values in the column.
     */
    public interface Column<T> {
        public String getTitle();
    }

    /**
     * Naive implementation of a column.
     */
    public static class ColumnImpl<E> implements Column<E> {

        private final String title;

        public ColumnImpl(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return title;
        }

    }

    @SuppressWarnings("serial")
    public static class DataPointOverriddenException extends RuntimeException {

        private final Column<?> column;
        private final Row<?> row;
        private final Object newValue;
        private final Object oldValue;

        private DataPointOverriddenException(Column<?> column, Row<?> row,
                Object newValue, Object oldValue) {
            this.column = column;
            this.row = row;
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        @Override
        public String getMessage() {
            return "DataPoint(" + column + ", " + row + ") had value: "
                    + oldValue + ". But was overridden with: " + newValue;
        }

    }

    /**
     * A representation of t row
     * 
     * @param <T>
     *            as the type of the index-value of the row
     */
    public interface Row<T> {
        public T getValue();
    }

    private class RowValueComparator implements Comparator<Row<R>> {
        private final Comparator<R> comparator;

        public RowValueComparator(Comparator<R> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Row<R> o1, Row<R> o2) {
            return comparator.compare(o1.getValue(), o2.getValue());
        }
    }

    /**
     * Naive implementation of a row.
     */
    public static class RowImpl<E> implements Row<E> {
        private final E value;

        public RowImpl(E value) {
            this.value = value;
        }

        @Override
        public E getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    // type invariant: the generic type of the column will always be the same
    // type as
    // the actual type of the object
    private Map<Column<?>, Map<Row<R>, Object>> table;
    private boolean consistencyCheckPerformed;
    private final List<Column<?>> columns;
    private final Comparator<R> rowComparator;

    /**
     * Constructor which defines the order of the rows and columns of the table.
     * 
     * The columns of a table are usually known beforehand.
     * 
     * The rows will be found dynamically, hence the comparator.
     * 
     * @param columns
     *            as the order of the columns
     * @param rowComparator
     *            as the comparator which desides the order of the rows
     */
    public ImprovedDataTable(List<Column<?>> columns,
            Comparator<R> rowComparator) {
        this.rowComparator = rowComparator;
        if (columns.isEmpty())
            throw new IllegalArgumentException("No columns");
        this.columns = new ArrayList<>(columns);
        this.table = new IdentityHashMap<>();
        for (Column<?> column : columns) {
            table.put(column, new IdentityHashMap<Row<R>, Object>());
        }
        this.consistencyCheckPerformed = false;
    }

    /**
     * Adds a datapoint at the (column, row)-coordinate. The type of the data
     * must be the same as that of the row.
     * 
     * @param column
     *            as the column to add to
     * @param row
     *            as the row to add to
     * @param data
     *            as the value to add
     * @throws DataPointOverriddenException
     *             if a value already is present at the coordinate
     */
    public <T> void addDataPoint(Column<T> column, Row<R> row, T data) {
        if (consistencyCheckPerformed)
            throw new IllegalStateException(
                    "Can not add more data points once the state has been queried once");
        Map<Row<R>, Object> dataColumn = table.get(column);
        if (dataColumn.containsKey(row))
            throw new DataPointOverriddenException(column, row, data,
                    dataColumn.get(row));
        // signature enforces the type invariant for the table
        dataColumn.put(row, data);

    }

    /**
     * Checks that this table is consistent: all datapoints are defined or null
     */
    private void consistencyCheck() {
        if (consistencyCheckPerformed)
            return;
        Set<Row<R>> rows = null;
        for (Entry<Column<?>, Map<Row<R>, Object>> entry : table.entrySet()) {
            if (rows == null)
                rows = entry.getValue().keySet();
            else if (!rows.equals(entry.getValue().keySet())) {
                throw new IllegalStateException(
                        "The columns does not have the same rows");
            }
        }
        consistencyCheckPerformed = true;
    }

    /**
     * @return the columns of the table in a predefined order
     */
    public List<Column<?>> getColumns() {
        consistencyCheck();
        return new ArrayList<>(columns);
    }

    /**
     * @return the rows of the table in a predefined order
     */
    public List<Row<R>> getRows() {
        consistencyCheck();
        // assumes that all colums have the same rows!
        // TODO add consistency check
        final ArrayList<Row<R>> list = new ArrayList<>(table.get(
                columns.get(0)).keySet());
        Collections.sort(list, new RowValueComparator(rowComparator));
        return list;
    }

    /**
     * Finds a value in the table at the desired coordinate
     * 
     * @param column
     *            as the column to find the value in
     * @param row
     *            as the row to find the value in
     * @return the value at (column, row) in the table
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Column<T> column, Row<?> row) {
        consistencyCheck();
        // use the table type invariant
        return (T) table.get(column).get(row);
    }
}

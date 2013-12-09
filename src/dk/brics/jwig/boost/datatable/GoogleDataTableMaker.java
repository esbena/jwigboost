package dk.brics.jwig.boost.datatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;

import dk.brics.jwig.boost.datatable.ImprovedDataTable.Column;
import dk.brics.jwig.boost.datatable.ImprovedDataTable.Row;

/**
 * Factory for creating a {@link DataTable} from a {@link ImprovedDataTable}
 * 
 * @param <R>
 *            as the type of the index-values of the rows
 */
public class GoogleDataTableMaker<R> {
	private Map<Column<?>, String> identifiers;
	private final ImprovedDataTable<R> table;
	private final Map<Column<?>, ValueType> columnTypes;

	/**
	 * Constructor with javascript-type-information for the columns
	 * 
	 * @param table
	 *            as the table to convert to google format
	 * @param columnTypes
	 *            as the javascript-type-information
	 */
	public GoogleDataTableMaker(ImprovedDataTable<R> table, Map<Column<?>, ValueType> columnTypes) {
		this.table = table;
		this.columnTypes = columnTypes;
		this.identifiers = new HashMap<>();
	}

	private String getIdentifier(Column<?> column) {
		if (!identifiers.containsKey(column))
			identifiers.put(column, "_" + identifiers.size());
		return identifiers.get(column);
	}

	/**
	 * Creates the table
	 * 
	 * @return the table
	 */
	public DataTable makeTable() {
		DataTable dataTable = new DataTable();
		List<ColumnDescription> cd = new ArrayList<>();
		for (Column<?> column : table.getColumns()) {
			cd.add(new ColumnDescription(getIdentifier(column), columnTypes.get(column), column.getTitle()));
		}

		dataTable.addColumns(cd);

		// Fill the data table.
		try {
			for (Row<R> row : table.getRows()) {
				List<Object> values = new ArrayList<>();
				for (Column<?> column : table.getColumns()) {
					values.add(table.getValue(column, row));
				}
				dataTable.addRowFromValues(values.toArray(new Object[values.size()]));
			}
		} catch (TypeMismatchException e) {
			throw new RuntimeException(e);
		}
		return dataTable;
	}
}

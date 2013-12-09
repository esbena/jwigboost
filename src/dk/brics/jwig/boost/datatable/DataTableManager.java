package dk.brics.jwig.boost.datatable;

import java.lang.ref.SoftReference;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.google.visualization.datasource.datatable.DataTable;

/**
 * Manager for handling the relation between {@link DataTable}s their generated
 * identifiers.
 */
class DataTableManager {
	private static DataTableManager instance;
	// let java handle the garbage collection of this infinitely growing map.
	// Might fail if it is GC'ed between a creation of a table and the request
	// for the table from the client
	private SoftReference<Map<DataTableToken, DataTable>> data;
	private final Random random;
	private static final Logger log = Logger.getLogger(DataTableManager.class);

	private DataTableManager() {
		// singleton
		this.data = new SoftReference<Map<DataTableToken, DataTable>>(new HashMap<DataTableToken, DataTable>());
		this.random = new SecureRandom();
	}

	public static DataTableManager get() {
		if (instance == null)
			instance = new DataTableManager();
		return instance;
	}

	public DataTableToken add(DataTable table) {
		final long nextLong = random.nextLong();

		DataTableToken token;
		while (true) {
			token = new DataTableToken("__datatabletoken__" + Math.abs(nextLong) + "__");
			ensureDataExists();
			if (!data.get().containsKey(token))
				break;
		}

		data.get().put(token, table);
		return token;
	}

	public DataTable getTable(DataTableToken token) {
		ensureDataExists();
		final DataTable dataTable = data.get().get(token);
		if (dataTable == null)
			log.info("Requested nonexisting datatable: " + token);
		return dataTable;
	}

	private void ensureDataExists() {
		if (data == null || data.get() == null)
			data = new SoftReference<Map<DataTableToken, DataTable>>(new HashMap<DataTableToken, DataTable>());
	}
}

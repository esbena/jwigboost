package dk.brics.jwig.boost.datatable;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.DataTableGenerator;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.Query;

import dk.brics.jwig.RequiredParameter;
import dk.brics.jwig.WebApp;
import dk.brics.jwig.WebSite;

/**
 * {@link WebApp} with a webmethod which supplies the data needed for
 * constructing a datatable in javascript.
 * 
 * A {@link WebSite} should instantiate this class and add it to the set of
 * active {@link WebApp}s.
 * 
 * @see DataTableBuilder
 * @see DataTableManager
 */
public class DataTableSupplier extends WebApp {
	public void get(@RequiredParameter final String token) throws IOException {
		DataSourceHelper.executeDataSourceServletFlow(getServletRequest(), getServletResponse(), new DataTableGenerator() {
			@Override
			public Capabilities getCapabilities() {
				return Capabilities.NONE;
			}

			@Override
			public DataTable generateDataTable(Query query, HttpServletRequest request) throws DataSourceException {
				final DataTable table = DataTableManager.get().getTable(new DataTableToken(token));
				return table;
			}
		}, false);
	}
}

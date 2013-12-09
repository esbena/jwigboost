package dk.brics.jwig.boost.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.visualization.datasource.datatable.DataTable;

public class CompleteDataTable {
    private final DataTable table;
    private final Map<String, String> properties;
    private final List<String> packages;
    private final String chartType;

    public CompleteDataTable(DataTable table, Map<String, String> properties,
            String chartType) {
        this.table = table;
        this.properties = properties;
        this.chartType = chartType;
        this.packages = new ArrayList<>();
        packages.add("corechart");
        packages.add("table");
    }

    public List<String> getPackages() {
        return packages;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public DataTable getTable() {
        return table;
    }

    public String getChartType() {
        return chartType;
    }
}

package dk.brics.jwig.boost.datatable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.visualization.datasource.datatable.DataTable;

import dk.brics.jwig.WebContext;
import dk.brics.jwig.boost.rendering.JavaScriptResource;
import dk.brics.jwig.boost.rendering.Resource;
import dk.brics.xact.XML;

/**
 * Creates the javascript and xml required to use a {@link DataTable} on a page.
 */
public class DataTableBuilder {
    private final Map<DataTableToken, CompleteDataTable> tables;
    private boolean done;

    public DataTableBuilder() {
        this.tables = new HashMap<>();
        done = false;
    }

    /**
     * Adds a data table, and returns the placeholder the datatable will be
     * rendered in
     */
    public XML add(CompleteDataTable table) {
        if (done)
            throw new RuntimeException(
                    "getResources() has already been called, do not add more tables!");
        DataTableToken token = DataTableManager.get().add(table.getTable());
        tables.put(token, table);
        final XML template = XML.parseTemplate("<div id=[ID]/>");
        return template.plug("ID", token.getToken());
    }

    /**
     * @return the resources required for rendering the tables.
     */
    public List<Resource> getResources() {
        done = true;
        List<Resource> resources = new ArrayList<>();
        resources.add(new JavaScriptResource("https://www.google.com/jsapi"));
        resources.add(makeScript(tables));
        return resources;
    }

    /**
     * creates a complese script tag which is responsible for rendering the
     * datatables
     */
    private Resource makeScript(Map<DataTableToken, CompleteDataTable> tables) {
        final String packages = makePackages(tables);
        final String preamble = "google.load('visualization', '1', {'packages':["
                + packages
                + "]});\n"
                + "google.setOnLoadCallback(render_google_chart_data);\n";

        final String init = "function render_google_chart_data(){"
                + makeInitBody(tables.keySet()) + "}\n";
        StringBuilder queryCallBacks = new StringBuilder();
        for (Entry<DataTableToken, CompleteDataTable> entry : tables.entrySet()) {
            queryCallBacks.append("function "
                    + makeCallBackName(entry.getKey()) + "(response){"
                    + makeQueryCallBackBody(entry) + "}\n");
        }
        final String script = queryCallBacks.toString() + init + preamble;

        return new Resource() {
            @Override
            public XML toXML() {
                return XML.parseTemplate(
                        "<script type='text/javascript'><[BODY]></script>")
                        .plug("BODY", script);
            }

        };
    }

    /**
     * canonical method for creating a callbackname
     */
    private String makeCallBackName(DataTableToken token) {
        return "queryCallBack_" + token.getToken();
    }

    /**
     * creates the body of a callback function.
     */
    private String makeQueryCallBackBody(
            Entry<DataTableToken, CompleteDataTable> entry) {
        String body = ""
                + "if (response.isError()) {\n"
                + "alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());\n"
                + "return;"
                + "}\n"//
                + "var data = response.getDataTable();\n"
                + "var chart = new google.visualization."
                + entry.getValue().getChartType()
                + "(document.getElementById('" + entry.getKey().getToken()
                + "'));" + "chart.draw(data, {"
                + makeProperties(entry.getValue().getProperties()) + "});\n";
        return body;
    }

    /**
     * creates a comma separated list of colon separated pairs of properties
     * 
     * <pre>
     * [[[x:y, x2:y2]]]
     * </pre>
     */
    private String makeProperties(Map<String, String> properties) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : properties.entrySet()) {
            sb.append("'" + entry.getKey() + "':'" + entry.getValue() + "',");
        }
        String propertiesString;
        if (sb.length() != 0) {
            // cut of trailing comma
            propertiesString = sb.subSequence(0, sb.length() - 1).toString();
        } else
            propertiesString = sb.toString();
        return propertiesString;
    }

    /**
     * creates the initbody, which sends a query per table
     * 
     * content: several of these:
     * 
     * <pre>
     * [[[var qX = ....Query(URL); qX.send(CALLBACK); ]]]
     * </pre>
     */
    private String makeInitBody(Set<DataTableToken> tokens) {
        // String baseURL = WebContext.getWebSiteURL().toString()
        // + "google_chart_data?token=";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (DataTableToken token : tokens) {
            final String tokenString = token.getToken();
            final URL url = WebContext.makeURL(DataTableSupplier.class, "get",
                    tokenString);
            sb.append("var q" + i + " = new google.visualization.Query('" + url
                    + "');\n");
            sb.append("q" + i + ".send(queryCallBack_" + tokenString + ");\n");
            i++;
        }
        return sb.toString();
    }

    /**
     * creates a comma separated list of the required packages
     * 
     * <pre>
     * [[[x, y, z]]]
     * </pre>
     */
    private String makePackages(Map<DataTableToken, CompleteDataTable> tables) {
        StringBuilder sb = new StringBuilder();
        Set<String> packages = new HashSet<>();
        for (Entry<DataTableToken, CompleteDataTable> entry : tables.entrySet()) {
            packages.addAll(entry.getValue().getPackages());
        }
        for (String p : packages) {
            sb.append("'" + p + "',");
        }
        String packagesString;
        if (sb.length() != 0) {
            // cut of trailing comma
            packagesString = sb.subSequence(0, sb.length() - 1).toString();
        } else {
            packagesString = sb.toString();
        }
        return packagesString;
    }
}

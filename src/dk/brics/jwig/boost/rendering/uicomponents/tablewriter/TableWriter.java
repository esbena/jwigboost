package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import dk.brics.xact.Element;
import dk.brics.xact.ToXMLable;
import dk.brics.xact.XML;
import org.apache.log4j.Logger;

/**
 * The table writer is responsible for writing tables all over the CourseAdmin
 * system. Tables show data to the user and may provide input boxed or other
 * features for every row in the table. A table is represented as a list of
 * columns controlled by {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableFeature} subclasses,
 * and any row corresponds to an object. The list of columns is given to the
 * constructor of the table writer and rows may be added on the fly. Finally a
 * {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.LineControlStrategy} may control whether a an
 * object spans multiple rows in the table created.
 * 
 * @author schwarz
 * @created 2008-12-18 16:33:59
 */
public class TableWriter<T> implements ToXMLable {
    private static Logger log = Logger.getLogger(ToXMLable.class);

    private static String translateSort(TableSortType sortType) {
        switch (sortType) {
        case DEFAULT:
            return "table-sortable:default";
        case ALPHANUMERIC:
            return "table-sortable:alphanumeric";
        case NUMERIC:
            return "table-sortable:numeric";
        case NUMERIC_COMMA:
            return "table-sortable:numeric_comma";
        case DATE:
            return "table-sortable:date";
        case NO_SORT:
            return "table-non-sortable";
        default:
            log.error("Unknown SortType, not implemented: " + sortType, new RuntimeException());
            return "table-non-sortable";
        }
    }

    private int counter;
    private LineControlStrategy<? super T> lineControlStrategy;
    protected List<? extends TableFeature<? super T>> features;
    private final List<T> rows = new LinkedList<>();
    private XML lastRow;
    private boolean reducedPadding;

    private boolean invertedColors = false;

    private boolean isLast;

    private XML firstRow;
    private Map<String, String> styles;

    /**
     * A table is transformable if the data is completely tabular so that other tools can transform the rows and cell.
     * Non-transformable table include tables that use a non-trivial line control strategy
     */
    private boolean transformable;

    private Set<Integer> summaryFeatures = new HashSet<>();

    /**
     * Creates a new table writer with the given
     * {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.LineControlStrategy} and the given set of
     * table columns
     * 
     * @param lineControlStrategy
     *            The line control strategy that should be used for this table
     * @param features
     *            The list of table columns that should be used for this table
     */
    public TableWriter(LineControlStrategy<? super T> lineControlStrategy,
            List<? extends TableFeature<? super T>> features) {
        this.lineControlStrategy = lineControlStrategy;
        this.features = features;
        counter = 0;
        styles = new HashMap<>();

        boolean orderAware = false;
        for (TableFeature<? super T> feature : features) {
            if (feature instanceof OrderAware)
                orderAware = true;
        }
        final boolean simpleLine = lineControlStrategy.getClass() == SimpleLineControlStrategy.class;
        if (!simpleLine || orderAware) {
            tablejsOn = false;
            transformable = false;
        } else {
            tablejsOn = true;
            transformable = true;
        }
    }

    /**
     * Whether the table.js library should be used. Is by default only used when
     * the SimpleLineControlStrategy is in use. Also, the use of any OrderAware
     * features disables this.
     * 
     * the default striping is applied regardless of the value of this variable,
     * table.js expects this to be done
     */
    private boolean tablejsOn;

    public void setTablejsOn(boolean on) {
        this.tablejsOn = on;
    }

    /**
     * Creates a new TableWriter with the columns given as argument. Uses a
     * {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.SimpleLineControlStrategy} as the strategy
     * 
     * @param features
     *            The list of table columns that should be used for this table
     */
    public TableWriter(List<? extends TableFeature<? super T>> features) {
        this(new SimpleLineControlStrategy(), features);
    }

    /**
     * Adds an object to the list of rows in the table. The data on the object
     * is not read until the table is written using the {@link #getTable()}
     * method.
     * 
     * @param row
     */
    public void addRow(T row) {
        rows.add(row);
    }

    /**
     * Get the XML for a single line in a table given one object
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public XML getBodyLine(T object) {
        XML row = XML.parseTemplate("<tr class=[CLASS]>" + "<[FIELDS]>"
                + "</tr>");
        LinkedList<XML> fields = new LinkedList<>();
        boolean startNewLine = lineControlStrategy.newLine(object);
        for (TableFeature<? super T> feature : features) {
            boolean isDominated = false;
            try {
                List<Method> getLines = new LinkedList<>();
                for (Method m : feature.getClass().getMethods()) {
                    if (m.getName().equals("getLine"))
                        getLines.add(m);
                }
                Class<?> actual = Object.class;
                for (Method m : getLines) {
                    Class<?>[] classes = m.getParameterTypes();
                    if (classes.length == 1) {
                        if (actual.isAssignableFrom(classes[0])) {
                            actual = classes[0];
                        }
                    }
                }
                Method getLine = feature.getClass()
                        .getMethod("getLine", actual);
                isDominated = getLine.getAnnotation(Dominated.class) != null;

            } catch (NoSuchMethodException e) {
                // Cannot happen
            }
            if (startNewLine || !isDominated) {
                if (feature instanceof OrderAware) {
                    OrderAware orderAware = (OrderAware) feature;
                    orderAware.setLast(isLast);
                }
                Object line = feature.getLine(object);
                if (line == null) {
                    line = "";
                }
                XML xml = null;
                if (line instanceof Element) {
                    Element x = (Element) line;
                    if (x.getLocalName().equals("td")) {
                        xml = x;
                    }
                }
                if (xml == null) {
                    xml = XML.parseTemplate(
                            "<td style=[STYLE] rowspan=[MEMBERS]><[A]></td>")
                            .plug("A", line);
                }
                if (!isDominated) {
                    xml = xml.plug("MEMBERS", "1");
                }
                if (feature instanceof HasCssStyle) {
                    HasCssStyle style = (HasCssStyle) feature;
                    xml = xml.plug("STYLE", style.getStyle());
                } else if (feature instanceof StyledTableFeature) {
                    xml = xml.plug("STYLE",
                            ((StyledTableFeature<T>) feature).getStyle());
                } else if (reducedPadding) {
                    xml = xml.plug("STYLE", "padding:10px");
                }

                fields.add(xml);

            }
        }
        XML fieldsXML = XML.concat(fields);
        if (startNewLine) {
            counter++;
            fieldsXML = fieldsXML.plug("MEMBERS",
                    lineControlStrategy.lineSize(object));
        }

        final String oddity = ((counter % 2 == 0) ^ !invertedColors) ? "even"
                : "odd";
        row = row.plug("CLASS", oddity);

        return row.plug("FIELDS", fieldsXML);
    }

    /**
     * Creates and returns the table from the objects added to the table so far.
     * Typically this is the method used to print the table to the user
     * 
     * @return
     */
    public XML getTable() {
        XML xml = XML
                .parseTemplate("<table data-transformable=[TRANSFORMABLE] data-summaryfeatures=[SUMMARY] class=[CLASS] style=[STYLE]>"//
                        + "<thead><[FIRSTROW]><tr class=\"header\" style=[PADDING]><[HEADERS]></tr><[FILTERS]></thead>"
                        + "<[LASTROW]>"
                        + "<tbody><[ROWS]></tbody>"
                        + "</table> ");
        xml = xml
                .plug("CLASS",
                        (tablejsOn ? "table-autosort table-autofilter table-stripeclass:even"
                                : "")
                                + " overview");
        xml = xml.plug("STYLE", flattenStyles());

        xml = xml.plug("TRANSFORMABLE", isTransformable());
        Set<Integer> summaryFeatures = new HashSet<>(this.summaryFeatures);
        if (summaryFeatures.isEmpty())
            summaryFeatures.add(0);
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (Iterator<Integer> iterator = summaryFeatures.iterator(); iterator.hasNext(); ) {
            Integer i = iterator.next();
            b.append(i);
            if (iterator.hasNext()) {
                b.append(",");
            }
        }
        b.append("]");
        xml = xml.plug("SUMMARY", b.toString());

        if (!hasEmptyHeaders()) {
            xml = xml.plug("HEADERS", writeHeaders());
            xml = xml.plug("FILTERS", writeFilters());
        }
        List<XML> xmls = new LinkedList<>();
        for (Iterator<T> it = rows.iterator(); it.hasNext();) {
            T row = it.next();
            isLast = !it.hasNext();
            xmls.add(getBodyLine(row));
        }
        xml = xml.plug("ROWS", XML.concat(xmls));
        if (firstRow != null) {
            xml = xml
                    .plug("FIRSTROW",
                            XML.parseTemplate(
                                    "<tr style='background-color:#F0F0F0'><td colspan=[SPAN] style='padding:0;'><[COL]></td></tr>")
                                    .plug("COL", firstRow)
                                    .plug("SPAN", features.size()));
        }
        if (lastRow != null) {
            xml = xml
                    .plug("LASTROW",
                            XML.parseTemplate(
                                    "<tfoot><tr><td colspan=[SPAN] style='padding:0;'><[COL]></td></tr></tfoot>")
                                    .plug("COL", lastRow)
                                    .plug("SPAN", features.size()));
        }
        if (reducedPadding) {
            xml = xml.plug("PADDING", "padding:5px");
        }
        return xml;
    }

    private String flattenStyles() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> style : styles.entrySet()) {
            sb.append(style.getKey() + ":" + style.getValue() + ";");
        }
        return sb.toString();
    }

    private boolean hasEmptyHeaders() {
        for (TableFeature<?> feature : features) {
            if (!feature.getCaption().equals(XML.parseTemplate("")))
                return false;
        }
        return true;
    }

    public boolean isReducedPadding() {
        return reducedPadding;
    }

    /**
     * Removes the specified row from the model, usefull for incremental changes
     * to a table.
     */
    public void removeRow(T row) {
        rows.remove(row);
    }

    public void setInvertedColors(boolean invert) {
        invertedColors = invert;
    }

    /**
     * Sets a piece of XML that will be the last row in the created table. This
     * is useful when for instance
     * 
     * @param lastRow
     */
    public void setLastRow(XML lastRow) {
        this.lastRow = lastRow;
    }

    /**
     * If set, the columns are squeezed tighter together to allow more data to
     * be shown
     * 
     * @param reducedPadding
     */
    public void setReducedPadding(boolean reducedPadding) {
        this.reducedPadding = reducedPadding;
    }

    public void setFirstRow(XML firstRow) {
        this.firstRow = firstRow;
    }

    /**
     * Implements the {@link dk.brics.xact.ToXMLable} interface by calling the
     * {@link #getTable()} method
     * 
     * @return
     */
    @Override
    public XML toXML() {
        return getTable();
    }

    private String translateFilter(boolean filterable) {
        if (filterable)
            return "table-filterable";
        return "table-non-filterable";
    }

    private XML writeFilters() {
        boolean hasFilter = false;
        List<XML> filters = new LinkedList<>();
        for (TableFeature<?> feature : features) {
            final boolean filterable = feature.isFilterable();
            XML filter = XML
                    .parseTemplate("<th class=[CLASS] style=[PADDING] ></th>");
            if (filterable)
                filter = filter.plug("CLASS", translateFilter(filterable));
            filters.add(filter);
            hasFilter = hasFilter || filterable;
        }
        XML xml;
        if (hasFilter && tablejsOn) {
            xml = XML
                    .parseTemplate("<tr class='header' style=[PADDING]><[FILTERS]></tr>");
            xml = xml.plug("FILTERS", XML.concat(filters));
            if (reducedPadding) {
                xml = xml.plug("PADDING", "padding:5px");
            }
        } else
            xml = XML.parseTemplate("");

        return xml;
    }

    /**
     * Create a piece of XML containing (only) the table headers of the table.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public XML writeHeaders() {
        List<XML> headers = new LinkedList<>();
        for (TableFeature<?> feature : features) {
            XML x = XML.parseTemplate(
                    "<th class=[CLASS] style=[STYLE] ><[A]></th>").plug("A",
                    feature.getCaption());
            x = x.plug("CLASS", translateSort(feature.getSortType()));
            if (feature instanceof HasCssStyle) {
                HasCssStyle style = (HasCssStyle) feature;
                x = x.plug("STYLE", style.getStyle());
            } else if (feature instanceof StyledTableFeature) {
                x = x.plug("STYLE",
                        ((StyledTableFeature<T>) feature).getStyle());
            } else if (reducedPadding) {
                x = x.plug("STYLE", "padding:5px");
            }
            headers.add(x);
        }

        return XML.concat(headers);
    }

    public void setStyle(String name, String value) {
        styles.put(name, value);
    }

    public boolean isTransformable() {
        return transformable;
    }

    public void setTransformable(boolean transformable) {
        this.transformable = transformable;
    }

    public void addSummaryFeature(Integer... featureIndex) {
        Collections.addAll(summaryFeatures, featureIndex);
    }

    @SafeVarargs
    public final void addSummaryFeature(TableFeature<? super T>... feature) {
        for (TableFeature<? super T> f : feature) {
            int i = features.indexOf(f);
            if (i != -1)
                summaryFeatures.add(i);
        }
    }
}

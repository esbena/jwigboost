package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

/**
 * A linke control strategy controls how new rows are inserted in the table. For any {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableFeature} annotated with
 * {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.Dominated} on the {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableFeature#getLine(Object)} method the method {@link #newLine(Object)}
 * method is invoked and if true is returned, a new line is created
 *
 * @author schwarz
 * @created 2008-12-18 14:50:50
 */
public interface LineControlStrategy<T> {
    /**
     * Returns true if a new row should be created in the table for the current object
     */
    public boolean newLine(T object);

    /**
     * Returns how many rows the object spans in the table
     */
    public int lineSize(T object);
}

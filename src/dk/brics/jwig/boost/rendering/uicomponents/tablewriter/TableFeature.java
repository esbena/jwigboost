package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

import dk.brics.xact.XML;

/**
 * A table feature is responsible for writing a single column in the {@link dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableWriter}.
 * Given some object of the class {@link E} the table feature must be able to return a piece of data that may
 * either be {@link dk.brics.xact.XML} or else be able to create a string representation using the {@link #toString()}
 * method
 *
 * @author schwarz
 * @created 2008-12-18 10:49:11
 */
public interface TableFeature<E>  {
    
    /**
     * Returns the caption of the column written by this table feature
     */
    XML getCaption();

    /**
     * Returns a representation of the given object that should be shown to the user. Invoking the getLine method
     * should return exactly what should be in the row of this object for the column represented by this
     * table feature.
     */
    public Object getLine(E object);
    
    public TableSortType getSortType();

    public boolean isFilterable();
}

package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

/**
 * Implementers can compute a class for each row in a {@link Table}. It can be safely assumed
 * that this computer is called exactly once per row in the table
 *
 * @author schwarz
 * @created 2008-10-17 11:26:51
 */
public interface RowClassComputer {
    public String computeClass();
}
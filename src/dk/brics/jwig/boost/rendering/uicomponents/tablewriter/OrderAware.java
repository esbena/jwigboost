package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

/**
 * @author schwarz
 * @created 2009-01-06 14:25:54
 */
public interface OrderAware {
    public boolean isLast();

    public void setLast(boolean last);
}

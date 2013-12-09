package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

/**
 * @author schwarz
 * @created 2008-12-18 14:52:02
 */
public class SimpleLineControlStrategy implements LineControlStrategy<Object> {
    @Override
    public int lineSize(Object o) {
        return 1;
    }

    @Override
    public boolean newLine(Object o) {
        return true;
    }
}

package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

/**
 * Common defaults for a {@link TableFeature}
 */
public abstract class DefaultTableFeature<T> implements TableFeature<T>{
    @Override
    public TableSortType getSortType(){
        return TableSortType.DEFAULT;
    }
    @Override
    public boolean isFilterable(){
        return true;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}

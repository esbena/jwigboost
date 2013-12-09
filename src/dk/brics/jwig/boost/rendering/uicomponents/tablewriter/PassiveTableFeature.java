package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;
/**
 * TableFeature which should not be sorted or filtered
 */
public abstract class PassiveTableFeature<T> implements TableFeature<T>{
    @Override
    public final TableSortType getSortType(){
        return TableSortType.NO_SORT;
    }
    @Override
    public final boolean isFilterable(){
        return false;
    }
}

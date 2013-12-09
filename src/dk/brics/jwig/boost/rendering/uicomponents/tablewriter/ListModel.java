package dk.brics.jwig.boost.rendering.uicomponents.tablewriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author schwarz
 * @created 2008-10-16 20:25:04
 */
public class ListModel<E> {
    private List<E> objects = new LinkedList<>();
    private boolean nullable;

    public ListModel(Collection<E> objects) {
        this.objects = new ArrayList<>(objects);
    }

    public ListModel(Collection<E> objects, boolean nullable) {
        this.objects = new ArrayList<>(objects);
        this.nullable = nullable;
    }

    public ListModel(List<E> objects, boolean nullable) {
        this.objects = objects;
        this.nullable = nullable;
    }

    public ListModel(List<E> objects) {
        this.objects = objects;
    }

    public void addObject(E object) {
        objects.add(object);
    }

    public void removeObject(E object) {
        objects.remove(object);
    }

    public List<E> getObjects() {
        return objects;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}

package dk.brics.jwig.boost.rendering.uicomponents.dropdownboxwriter;


public interface DropdownFeature<T> {

    String serializeObject(T item);

    Object renderObject(T item);

}

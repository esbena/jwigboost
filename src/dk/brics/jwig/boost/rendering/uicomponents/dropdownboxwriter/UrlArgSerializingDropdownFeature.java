package dk.brics.jwig.boost.rendering.uicomponents.dropdownboxwriter;

import dk.brics.jwig.server.RequestManager;

public abstract class UrlArgSerializingDropdownFeature<E> implements
        DropdownFeature<E> {
    @Override
    public String serializeObject(E item) {
        if (item == null)
            return "noneSelected";
        return RequestManager.makeURLArg(item);
    }
}

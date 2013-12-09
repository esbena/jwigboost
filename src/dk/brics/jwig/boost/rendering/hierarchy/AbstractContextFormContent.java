package dk.brics.jwig.boost.rendering.hierarchy;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for creating a context.
 */
public abstract class AbstractContextFormContent implements ContextFormContent {
    private final Map<String, Object> map;

    protected AbstractContextFormContent() {
        map = new HashMap<>();
    }

    protected void addContext(String name, Object value) {
        if (name == null)
            throw new RuntimeException("The context name can not be null");
        if (value == null)
            throw new RuntimeException("'" + name
                    + "' can not have null as context");
        if ("submit".equals(name))
            throw new IllegalArgumentException(
                    "The name should not be 'submit', it will collide with the form submission");
        Object old = map.put(name, value);
        if (old != null)
            throw new IllegalArgumentException(
                    "The context can not contain the same name twice.");
    }

    @Override
    public final Map<String, Object> getContext() {
        return map;
    }

}

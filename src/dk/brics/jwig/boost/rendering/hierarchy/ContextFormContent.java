package dk.brics.jwig.boost.rendering.hierarchy;

import java.util.Map;

/**
 * A form with a context.
 */
public interface ContextFormContent extends FormContent {

    /**
     * The context is a map from names to values.
     * 
     * @return the context
     */
    public Map<String, Object> getContext();

}

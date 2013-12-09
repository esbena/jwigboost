package dk.brics.jwig.boost.rendering.hierarchy;

import dk.brics.xact.XML;

public interface JSGuardFormContent extends FormContent {
    /**
     * Returns the JS to be placed before the form element.
     * 
     * @return JS represented as XML.
     */
    public XML getJSGuard();
}

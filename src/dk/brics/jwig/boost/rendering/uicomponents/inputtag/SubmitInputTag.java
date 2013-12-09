package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

public class SubmitInputTag extends InputTag {

    public SubmitInputTag(String name, Object value, Attribute... attributes) {
        super("submit", name, value, attributes);
    }

    public SubmitInputTag(String name, Object value, Object relator,
            Attribute... attributes) {
        super("submit", name, value, relator, attributes);
    }

}

package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

public class ButtonInputTag extends InputTag {
    public ButtonInputTag(String variableName, Object value,
            Attribute attributes) {
        super("button", variableName, value, attributes);
    }
}

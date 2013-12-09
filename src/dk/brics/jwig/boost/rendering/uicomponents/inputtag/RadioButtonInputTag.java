package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

public class RadioButtonInputTag extends InputTag {
    /**
     * RadioButton Input tag for some object
     * 
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     */
    public RadioButtonInputTag(String variableName, Object value,
            boolean selected, Attribute... attributes) {
        this(variableName, value, null, selected, attributes);
    }

    /**
     * RadioButton Input tag for some object, related to some object (most
     * likely the same)
     * 
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     */
    public RadioButtonInputTag(String variableName, Object value,
            Object relator, boolean selected, Attribute... attributes) {
        super("radio", variableName, value, relator, Util.addChecked(
                attributes, selected));
    }

}

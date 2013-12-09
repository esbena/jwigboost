package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;
import dk.brics.xact.XML;

/**
 * Allows easy construction of input tags with a 'selected' attribute.
 * 
 * @see InputTag
 */
public class CheckBoxInputTag extends InputTag {

    /**
     * CheckBox input tag without a relation to another object
     * 
     * @see InputTag#InputTag(String, String, Object, Attribute...)
     */
    public CheckBoxInputTag(String variableName,
            boolean selected, Attribute... attributes) {
        this(variableName, null, selected, attributes);
    }

    /**
     * CheckBox input tag with a relation to another object
     * 
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     */
    public CheckBoxInputTag(String variableName,
            Object relator, boolean selected, Attribute... attributes) {
        super("checkbox", variableName, "", relator, Util.addChecked(
                attributes,
                selected));
    }
    
    @Override
    public XML toXML() {
        return super.toXML().remove("//@value");
    }
}

package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

public class TextInputTag extends InputTag {
    /**
     * Text input tag without a relation to anothen object
     * 
     * @see InputTag#InputTag(String, String, Object, Attribute...)
     */
    public TextInputTag(String variableName, Object value,
            Attribute... attributes) {
        this(variableName, value, null, attributes);
    }

    /**
     * Text input tag with a relation to anothen object
     * 
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     */
    public TextInputTag(String variableName, Object value, Object relator,
            Attribute... attributes) {
        super("text", variableName, value, relator, attributes);
    }
}

package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

public class PasswordInputTag extends InputTag {
    /**
     * Input tag without a relation to anothen object
     * 
     * @see InputTag#InputTag(String, String, Object, Attribute...)
     */
    public PasswordInputTag(String variableName, Object value,
            Attribute... attributes) {
        super("password", variableName, value, null, attributes);
    }
}

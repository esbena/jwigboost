package dk.brics.jwig.boost.rendering.uicomponents.inputtag;


public class HiddenInputTag extends InputTag {
    /**
     * Hidden input tag without a relation to another object
     * 
     * @see InputTag#InputTag(String, String, Object)
     */
    public HiddenInputTag(String variableName, Object value) {
        this(variableName, value, null);
    }

    /**
     * Hidden input tag with a relation to another object
     * 
     * @see InputTag#InputTag(String, String, Object, Object)
     */
    public HiddenInputTag(String variableName, Object value, Object relator) {
        super("hidden", variableName, value, relator);
    }
}

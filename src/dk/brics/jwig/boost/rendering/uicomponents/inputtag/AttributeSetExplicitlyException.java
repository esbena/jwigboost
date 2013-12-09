package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

@SuppressWarnings("serial")
public class AttributeSetExplicitlyException extends RuntimeException {

    public AttributeSetExplicitlyException(String attribute) {
        super(
                "The attribute '"
                        + attribute
                        + "' is set automatically by this class, and should not be set by hand.");
    }
}

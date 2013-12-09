package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;
import dk.brics.jwig.server.RequestManager;
import dk.brics.xact.ToXMLable;
import dk.brics.xact.XML;

/**
 * Allows easy construction of input tags:
 * 
 * <pre>
 * <input type='xxx' name='yyy' value='zzz' .../>
 * </pre>
 * 
 * Also supports inputs with a relation to another object, such as the name of a
 * specific person.
 */
public abstract class InputTag implements ToXMLable {

    private final String type;
    private final String name;
    private final Object value;
    private final Object relator;
    private final Map<String, Object> attributes;

    private final static XML template = XML.parseTemplate("" + //
            "<input type=[__INPUT__] " + //
            "name=[__NAME__] value=[__VALUE__] " + //
            "class=[__CLASS__] onclick=[__ONCLICK__] " + //
            "onchange=[__ONCHANGE__] hidden=[__HIDDEN__] " + //
            "checked=[__CHECKED__] " + //
            "placeholder=[__PLACEHOLDER__]" + //
            "size=[__SIZE__]" + //
            "id=[__ID__] />");
    private static Set<String> rejectedAttributes;
    private static Set<String> acceptedAttributes;

    /**
     * An input which is not related to a particular object.
     * 
     * Example, where the id of X1 is 1. <br/>
     * <code>new InputTag("foo", "bar", new X1(), new Attribute("class",
     * "baz"))</code><br/>
     * results in: <br/>
     * <code>
     * &lt;input type='foo' name='bar' value='1' class='baz'/&gt;
     * </code>
     * 
     * @see InputTag#InputTag(String, String, Object, Object, Attribute...)
     * @param type
     *            as the type of the input tag
     * @param variableName
     *            as the name of the input tag
     * @param value
     *            as the value of the input tag
     * @param attributes
     *            as the extra attributes to set (optional)
     */
    public InputTag(String type, String variableName, Object value,
            Attribute... attributes) {
        this(type, variableName, value, null, attributes);
    }

    /**
     * An input tag.<br/>
     * <br/>
     * Example, where the id of X1 and X2 is 1 and 2 respectively. <br/>
     * <code>new InputTag("foo", "bar", new X1(), new X2(), new Attribute("class",
     * "baz"))</code><br/>
     * results in: <br/>
     * <code>
     * &lt;input type='foo' name='2:bar' value='1' class='baz'/&gt;
     * </code>
     * 
     * @see InputTag#InputTag(String, String, Object, Attribute...)
     * @param type
     *            as the type of the input tag
     * @param variableName
     *            as the (perhaps partial) name of the input tag
     * @param value
     *            as the value of the input tag
     * @param relator
     *            as the other part (if not null) of the name of the input tag
     * @param attributes
     *            as the extra attributes to set (optional)
     */
    public InputTag(String type, String variableName, Object value,
            Object relator, Attribute... attributes) {
        this.type = type;
        this.name = variableName;
        this.value = value;
        this.relator = relator;
        this.attributes = Attribute.convertAttributeArrayToMap(attributes);
        checkValues();
    }

    /**
     * Checks the validity of the user-set attributes.
     */
    private void checkAttributes(Map<String, Object> attributes) {
        Set<String> usedAttributes = new HashSet<>();
        usedAttributes.addAll(attributes.keySet());

        usedAttributes.removeAll(getAcceptedAttributes());

        if (!usedAttributes.isEmpty())
            throw new IllegalArgumentException("Unsupported attribute: "
                    + usedAttributes.iterator().next());

        for (String rejectedAttribute : getRejectedAttributes()) {
            if (usedAttributes.contains(rejectedAttribute))
                throw new AttributeSetExplicitlyException(rejectedAttribute);
        }

        for (Entry<String, Object> entry : attributes.entrySet()) {
            if (entry.getValue() == null)
                throw new IllegalArgumentException(entry.getKey()
                        + " can not be null");
        }
    }

    private static Set<String> getAcceptedAttributes() {
        if (acceptedAttributes == null) {
            acceptedAttributes = new HashSet<>();
            acceptedAttributes.add("class");
            acceptedAttributes.add("size");
            acceptedAttributes.add("onclick");
            acceptedAttributes.add("hidden");
            acceptedAttributes.add("checked");
            acceptedAttributes.add("onchange");
            acceptedAttributes.add("placeholder");
            acceptedAttributes.add("id");
        }
        return acceptedAttributes;
    }

    private static Set<String> getRejectedAttributes() {
        if (rejectedAttributes == null) {
            rejectedAttributes = new HashSet<>();
            rejectedAttributes.add("type");
            rejectedAttributes.add("name");
            rejectedAttributes.add("value");
        }
        return rejectedAttributes;
    }

    /**
     * Checks the values of this object, to report errors as soon as possible.
     */
    private void checkValues() {
        if (type == null)
            throw new IllegalArgumentException("type can not be null");
        if (name == null)
            throw new IllegalArgumentException("name can not be null");
        if (value == null)
            throw new IllegalArgumentException("value can not be null");
        checkAttributes(attributes);
    }

    /**
     * Constructs the name of the input tag.
     */
    private String makeName(String name, Object relator) {
        if (relator == null)
            return name;
        String compositeName = name + ":" + RequestManager.makeURLArg(relator);
        return compositeName;
    }

    @Override
    public XML toXML() {
        XML xml = template;
        xml = xml.plug("__INPUT__", type);
        xml = xml.plug("__NAME__", makeName(name, relator));
        xml = xml.plug("__VALUE__", value);
        for (Entry<String, Object> entry : attributes.entrySet()) {
            xml = xml.plug("__" + entry.getKey().toUpperCase() + "__",
                    entry.getValue());
        }
        return xml;
    }
}

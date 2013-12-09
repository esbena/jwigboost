package dk.brics.jwig.boost.rendering.uicomponents;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A name-value pair.
 */
public class Attribute {
    private final String name;
    private final Object value;

    public Attribute(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Utility method for converting back and forth between attribute representations
     */
    public static Map<String, Object> convertAttributeArrayToMap(
            Attribute[] attributes) {
        Map<String, Object> map = new HashMap<>();
        if (attributes != null) {
            for (Attribute attribute : attributes)
                map.put(attribute.getName(), attribute.getValue());
        }
        return map;
    }

    /**
     * Utility method for converting back and forth between attribute representations
     */
    public static Attribute[] convertAttributeMapToArray(Map<String, Object> map) {
        Attribute[] newAttributes = new Attribute[map.size()];
        int i = 0;
        for (Entry<String, Object> entry : map.entrySet()) {
            newAttributes[i] = new Attribute(entry.getKey(), entry.getValue());
            i++;
        }
        return newAttributes;
    }

    /**
     * Utility method for getting the value of a specific attribute
     */
    public static Object getAttributeValue(Attribute[] attributes, String name) {
        return convertAttributeArrayToMap(attributes).get(name);
    }

    /**
     * Utility method for setting the value of a specific attribute
     */
    public static Attribute[] setAttributeValue(Attribute[] attributes, String name, Object value) {
        Map<String, Object> map = convertAttributeArrayToMap(attributes);
        map.put(name, value);
        final Attribute[] modified = convertAttributeMapToArray(map);
        return modified;
    }

    /**
     * Utility method for adding an extra class name to the class-attribute.
     */
    public static Attribute[] addClass(Attribute[] attributes, String className) {
        Object oldClass = Attribute.getAttributeValue(attributes, "class");
        final String newClass;
        if (oldClass != null) {
            newClass = String.format("%s %s", oldClass.toString(), className);
        } else {
            newClass = className;
        }
        return Attribute.setAttributeValue(attributes, "class", newClass);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}

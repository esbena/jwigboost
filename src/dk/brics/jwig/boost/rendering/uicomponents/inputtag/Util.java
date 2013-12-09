package dk.brics.jwig.boost.rendering.uicomponents.inputtag;

import java.util.Map;

import dk.brics.jwig.boost.rendering.uicomponents.Attribute;

public class Util {
    static Attribute[] addChecked(Attribute[] attributes, boolean selected) {
        Map<String, Object> map = Attribute
                .convertAttributeArrayToMap(attributes);

        if (map.containsKey("checked")) {
            throw new AttributeSetExplicitlyException("checked");
        }
        if (selected) {
            map.put("checked", "checked");
        }

        Attribute[] newAttributes = Attribute.convertAttributeMapToArray(map);
        return newAttributes;
    }
}

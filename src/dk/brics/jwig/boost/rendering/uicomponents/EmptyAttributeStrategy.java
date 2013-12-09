package dk.brics.jwig.boost.rendering.uicomponents;

import dk.brics.xact.AttrNode;

import java.util.Collection;
import java.util.Collections;

public class EmptyAttributeStrategy implements AttributeStrategy {
    @Override
    public Collection<? extends AttrNode> getAttributes() {
        return Collections.<AttrNode>emptySet();
    }
}

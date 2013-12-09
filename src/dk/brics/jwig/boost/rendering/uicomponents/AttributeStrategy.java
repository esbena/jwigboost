package dk.brics.jwig.boost.rendering.uicomponents;

import dk.brics.xact.AttrNode;

import java.util.Collection;

public interface AttributeStrategy {
    public Collection<? extends AttrNode> getAttributes();
}

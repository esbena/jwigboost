package dk.brics.jwig.boost.rendering.hierarchy;

import java.util.List;

import dk.brics.jwig.boost.rendering.Resource;

public interface HasOwnResources extends Renderable {
    public List<Resource> getResources();
}

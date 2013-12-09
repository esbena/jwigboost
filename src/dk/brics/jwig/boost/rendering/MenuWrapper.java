package dk.brics.jwig.boost.rendering;

import java.util.List;

import dk.brics.xact.XML;

public interface MenuWrapper {
    public XML menuwrap(XML body, XML title);

    public List<Resource> getResources();
}

package dk.brics.jwig.boost.rendering.hierarchy;

import dk.brics.xact.XML;

/**
 * An elemnent with a title.
 */
public interface Titled extends Renderable {
    /**
     * @return the title
     */
    public XML getTitle();
}

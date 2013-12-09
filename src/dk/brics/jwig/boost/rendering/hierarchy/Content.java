package dk.brics.jwig.boost.rendering.hierarchy;

import dk.brics.xact.XML;

/**
 * An element with some content
 */
public interface Content extends Renderable {
    /**
     * @return the content
     */
    public XML getContent();

}

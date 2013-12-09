package dk.brics.jwig.boost.exceptions;

import dk.brics.jwig.boost.rendering.hierarchy.Content;
import dk.brics.jwig.boost.rendering.hierarchy.Titled;
import dk.brics.xact.XML;

@SuppressWarnings("serial")
public class RenderableException extends RuntimeException implements Content,
        Titled {

    private final XML content;
    private final XML title;

    public RenderableException(XML title, XML content) {
        this.content = content;
        this.title = title;
    }

    @Override
    public XML getContent() {
        return content;
    }

    @Override
    public XML getTitle() {
        return title;
    }
}

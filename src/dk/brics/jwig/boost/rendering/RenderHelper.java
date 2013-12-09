package dk.brics.jwig.boost.rendering;

import dk.brics.jwig.boost.rendering.hierarchy.Content;
import dk.brics.jwig.boost.rendering.hierarchy.Renderable;
import dk.brics.jwig.boost.rendering.hierarchy.Titled;
import dk.brics.xact.XML;

public class RenderHelper {
    public static Renderable makeRenderable(final XML xml) {
        return new Content() {

            @Override
            public XML getContent() {
                return xml;
            }

        };
    }
    
    public static Renderable makeRenderable(Object body) {
        return makeRenderable(XML.parseTemplate("<[VAL]>").plug("VAL", body));
    }
    
    private static class TitledContent implements Content, Titled {
        
        private final XML content, title;

        public TitledContent(XML content, XML title) {
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
    
    public static Renderable makeRenderable(Object body, Object title) {
        if (title == null) {
            return makeRenderable(body);
        } else {
            return new TitledContent(XML.parseTemplate("<[VAL]>").plug("VAL",
                    body), XML.parseTemplate("<[VAL]>").plug("VAL", title));
        }
    }
}

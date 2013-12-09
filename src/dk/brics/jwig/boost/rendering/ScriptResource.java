package dk.brics.jwig.boost.rendering;

import dk.brics.xact.XML;

public abstract class ScriptResource implements Resource {
    private final XML value;
    private static final XML template = XML
            .parseTemplate("<script type=[TYPE] src=[SRC]></script>");

    // <script type="text/javascript" src=[JQUERY] />

    @Override
    public XML toXML() {
        return value;
    }

    public ScriptResource(String src, String type) {
        value = template.plug("TYPE", type).plug("SRC", src);
    }

}

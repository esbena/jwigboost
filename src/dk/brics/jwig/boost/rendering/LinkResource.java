package dk.brics.jwig.boost.rendering;

import dk.brics.xact.XML;

public abstract class LinkResource implements Resource {
    private final XML value;
    private final static XML template = XML
            .parseTemplate("<link href=[HREF] rel=[REL] type=[TYPE] media=[MEDIA]></link>");

    // <link href=[URL] rel="stylesheet" type="text/css" />
    public LinkResource(String href, String rel, String type, String media) {
        //media cannot be "". Do not plug it if it is missing
        XML value = template.plug("HREF", href).plug("REL", rel).plug("TYPE", type);
        if (media != null)
            value = value.plug("MEDIA", media);
        this.value = value;
    }

    public LinkResource(String href, String rel, String type) {
        this(href, rel, type, null);
    }

    @Override
    public XML toXML() {
        return value;
    }

}

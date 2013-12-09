package dk.brics.jwig.boost.rendering;


public class CSSLinkResource extends LinkResource {
    public CSSLinkResource(String href) {
        super(href, "stylesheet", "text/css");
    }

    public CSSLinkResource(String href, String media) {
        super(href, "stylesheet", "text/css", media);
    }
}

package dk.brics.jwig.boost.rendering.uicomponents;

import java.net.URL;

import dk.brics.xact.XML;

public class OpenPopupBox {

    public static final XML CLOSE_POPUP = XML
            .parseTemplate("<script type='text/javascript'>window.parent.Popup.closeCurrent();</script>");

    private String url;
    private String title;

    private boolean fullscreen;
    private int width = 800;
    private int height = 600;

    private boolean closebutton = true;

    /**
     * Open the popup box with the URL/title from the enclosing link.
     */
    public OpenPopupBox() {
        // Nothing...
    }

    public OpenPopupBox(int width, int height, boolean closebutton) {
        this.width = width;
        this.height = height;
        this.closebutton = closebutton;
    }
    
    /**
     * Open popup box in fullscreen.
     */
    public OpenPopupBox(boolean closebutton) {
        this.fullscreen = true;
        this.closebutton = closebutton;
    }

    /**
     * Open the popup box with the URL from the enclosing link.
     */
    public OpenPopupBox(String title) {
        this.title = title;
    }
    
    public OpenPopupBox(String title, int width, int height, boolean closebutton) {
        this.width = width;
        this.height = height;
        this.closebutton = closebutton;
    }

    public OpenPopupBox(String title, boolean closebutton) {
        this.fullscreen = true;
        this.closebutton = closebutton;
    }

    /**
     * Create a new popup box displaying the content located at the URL.
     * 
     * @param title
     *            The title of the popup box
     */
    public OpenPopupBox(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public OpenPopupBox(String title, URL url) {
        this(title, url.toString());
    }

    public OpenPopupBox(String title, String url, int width, int height) {
        this(title, url);

        this.width = width;
        this.height = height;
    }

    public OpenPopupBox(String title, URL url, int width,
            int height) {
        this(title, url.toString(), width, height);
    }

    public OpenPopupBox(String title, URL url, int width,
            int height, boolean closebutton) {
        this(title, url, width, height);
        this.closebutton = closebutton;
    }

    @Override
    public String toString() {
        String dim;
        if (fullscreen) {
            dim = "Popup.fullscreen";
        } else {
            dim = "Popup.fixedSize(" + width + ", " + height + ")";
        }

        String title;
        if (this.title != null) {
            title = "'" + this.title + "'";
        } else {
            title = "$(this).text()";
        }

        String url;
        if (this.url != null) {
            url = "'" + this.url.toString() + "'";
        } else {
            url = "$(this).attr('href')";
        }

        return "var popup = new Popup(" + title + ", " + url
                + ", { dimensions: " + dim
                + ", closebutton: " + closebutton + " }); "
                + "return popup.open();";
    }

}

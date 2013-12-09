package dk.brics.jwig.boost.exceptions;

import javax.servlet.http.HttpServletResponse;

import dk.brics.jwig.boost.rendering.EALoginArea;
import dk.brics.xact.XML;

@SuppressWarnings("serial")
public class EALoginRequiredException extends RenderableException implements
        HasStatusCode, DoNotLogThisException {

    public EALoginRequiredException() {
        super(XML.parseTemplate("<[s_Login_Required]>"), XML.parseTemplate(
                "<[VALUE]>").plug("VALUE",
                new EALoginArea().create().getValue()));
    }

    @Override
    public int getStatusCode() {
        return HttpServletResponse.SC_FORBIDDEN;
    }

}

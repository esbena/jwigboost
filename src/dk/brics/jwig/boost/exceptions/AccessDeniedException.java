package dk.brics.jwig.boost.exceptions;

import javax.servlet.http.HttpServletResponse;

import dk.brics.xact.XML;

@SuppressWarnings("serial")
public class AccessDeniedException extends RenderableException implements
        HasStatusCode, DoNotLogThisException {

    public AccessDeniedException(XML explanation) {
        super(XML.parseTemplate("<[s_Accessdenied]>"), XML.parseTemplate(
                "<[EXPLANATION]>").plug("EXPLANATION", explanation));
    }

    @Override
    public int getStatusCode() {
        return HttpServletResponse.SC_FORBIDDEN;
    }
}

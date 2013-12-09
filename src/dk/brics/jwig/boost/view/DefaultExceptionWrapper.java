package dk.brics.jwig.boost.view;

import dk.brics.jwig.boost.RootApp;
import dk.brics.jwig.boost.exceptions.ErrorLogger;
import dk.brics.jwig.boost.rendering.ExceptionWrapper;
import dk.brics.xact.XML;

public class DefaultExceptionWrapper implements ExceptionWrapper {

    @Override
    public XML getErrorMessage(Exception e) {
        XML xml = XML.parseTemplateResource(DefaultExceptionWrapper.class,
                "error.xml");

        final XML errorMessage = RootApp.isDebug()
                ? ErrorLogger.prettyPrintError(e)
                : XML.parseTemplate("");

        xml = xml.plug("TITLE", XML.parseTemplate("<[s_Internal_Error]>"));
        xml = xml.plug("BODY", errorMessage);

        return xml;
    }

}

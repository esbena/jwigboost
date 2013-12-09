package dk.brics.jwig.boost.exceptions;

import dk.brics.jwig.Email;
import dk.brics.jwig.WebApp;
import dk.brics.jwig.WebContext;
import dk.brics.jwig.boost.RootApp;
import dk.brics.jwig.boost.email.EmailHelper;
import dk.brics.xact.XML;
import org.apache.log4j.Logger;

import java.util.*;

public class ErrorLogger {
    public class ErrorDescription {

        public Date getDate() {
            return date;
        }

        public String getRequestURL() {
            return requestURL;
        }

        public Exception getException() {
            return exception;
        }

        private final Date date;
        private final Exception exception;
        private final String requestURL;

        public ErrorDescription(Date date, String requestURL,
                                Exception exception) {
            this.date = date;
            this.requestURL = requestURL;
            this.exception = exception;
        }

    }

    private static ErrorLogger instance;
    private final List<ErrorDescription> errors;
    @SuppressWarnings("unused")
    private final WebApp app;

    private static Logger log = Logger.getLogger(ErrorLogger.class);

    private ErrorLogger(WebApp app) {
        // singleton
        errors = Collections
                .synchronizedList(new ArrayList<ErrorDescription>());
        this.app = app;
    }

    public static void instantiate(WebApp app) {
        instance = new ErrorLogger(app);
    }

    public static ErrorLogger get() {
        if (instance == null)
            throw new RuntimeException("not yet instantiated");
        return instance;
    }

    public void log(Exception e) {
        String requestURL = WebContext.getRequestURL().toString();

        if (!(e instanceof UrgentException)) {
            errors.add(new ErrorDescription(new Date(), requestURL, e));
            if (errors.size() > 10) {
                List<ErrorDescription> errorsCopy = null;
                synchronized (this) {
                    errorsCopy = new ArrayList<>(errors);
                    errors.clear();
                }
                reportErrors(errorsCopy);
            }
        } else {
            reportErrors(Collections.singletonList(new ErrorDescription(
                    new Date(), requestURL, e)));
        }
    }

    public static XML prettyPrintError(Exception e) {
        final int traceLinesToPrint = 5;

        XML xml = XML.parseTemplate("<[MSG]><[TRACE]>");
        xml = xml.plug("MSG", e.toString());

        StackTraceElement[] stackTrace = e.getStackTrace();
        XML lineTemplate = XML.parseTemplate("<div><[LINE]></div>");
        List<XML> lineList = new ArrayList<>();
        final String message = e.getMessage();
        final String errorName = e.getClass().getName();
        final String errorMessage = message == null ? "" : message;
        lineList.add(lineTemplate.plug("LINE", errorName + ": " + errorMessage));
        for (int i = 0; i <= traceLinesToPrint; i++) {
            if (i != traceLinesToPrint)
                lineList.add(lineTemplate.plug("LINE", stackTrace[i].toString()));
            else
                lineList.add(lineTemplate.plug("LINE", "... More in log ..."));
        }
        return xml.plug("TRACE", XML.concat(lineList));
    }

    private void reportErrors(List<ErrorDescription> errors) {
        final String receiver = app.getProperty("course.staffemail");

        try {
            if (!RootApp.isDebug()) {
                Email email = EmailHelper.makeEmail(
                        "Please view this email as HTML...",
                        makeMessage(errors), "Recent bugs at courseadmin",
                        receiver);
                app.sendEmail(email);
            }
        } catch (Exception e) {
            // Report error to log
            log.error(e);
        }
    }

    @SuppressWarnings("unused")
    private XML makeMessage(List<ErrorDescription> errors) {
        List<XML> xmlErrors = new LinkedList<>();
        final XML errorTemplate = XML
                .parseTemplate("<div><h3><[TITLE]></h3><div><[URL]></div><div><[TRACE]></div></div>");
        for (ErrorDescription error : errors) {
            final XML trace = prettyPrintError(error.getException());
            final Date date = error.getDate();

            XML message = errorTemplate.plug("TITLE", date)
                    .plug("TRACE", trace);

            if (error.getRequestURL() != null) {
                message = message.plug("URL", error.getRequestURL());
            }

            xmlErrors.add(message);
        }
        final XML completeXML = XML.parseTemplate("<div><[BODY]></div>").plug(
                "BODY", XML.concat(xmlErrors));
        return completeXML;
    }

}

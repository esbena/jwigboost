package dk.brics.jwig.boost;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import dk.brics.jwig.JWIGException;
import dk.brics.jwig.Priority;
import dk.brics.jwig.Response;
import dk.brics.jwig.URLPattern;
import dk.brics.jwig.WebApp;
import dk.brics.jwig.WebSite;
import dk.brics.jwig.boost.exceptions.DoNotLogThisException;
import dk.brics.jwig.boost.exceptions.ErrorLogger;
import dk.brics.jwig.boost.exceptions.HasStatusCode;
import dk.brics.jwig.boost.exceptions.Redirect;
import dk.brics.jwig.boost.rendering.ExceptionWrapper;
import dk.brics.jwig.boost.rendering.MenuWrapper;
import dk.brics.jwig.boost.rendering.hierarchy.Renderable;
import dk.brics.jwig.boost.rendering.hierarchy.Renderer;
import dk.brics.jwig.boost.rendering.injection.InjectCommand;
import dk.brics.jwig.boost.rendering.injection.InjectCommandFactory;
import dk.brics.jwig.boost.view.DefaultExceptionWrapper;
import dk.brics.jwig.server.ThreadContext;
import dk.brics.jwig.util.URLEncoding;
import dk.brics.xact.Attribute;
import dk.brics.xact.Element;
import dk.brics.xact.NamespaceDecl;
import dk.brics.xact.XML;

public abstract class RootApp extends WebApp {

    private class RootAppResponseData {
        private RootAppResponseData(String currentLang) {
            this.currentLang = currentLang;
        }
        String currentLang;
    }

    protected static final int MAX_PRIORITY_BOOST = PRE_CACHE + 1000; // 2000
    protected static final int LOCALIZATION = PRE_CACHE + 200; // 1200
    protected static final int REDIRECT_TO_SECURE = MAX_PRIORITY_BOOST + 1; // 2001

    public static String getDateFormat() {
        WebApp webApp = get();
        if (webApp == null) {
            webApp = ThreadContext.getRequestManagers().get(0).getWebApp();
        }
        return webApp.getProperty("dateformat","dd/MM/yyyy HH:mm");
    }

    public static String getLang() {
        if (getResponse().hasResponseScopeData(RootAppResponseData.class)) {
            RootAppResponseData responseScopeData = getResponse().getResponseScopeData(RootAppResponseData.class);
            return responseScopeData.currentLang;
        } else
            return Localizer.get().getDefaultLang();
    }

    private final Localizer localizer;

    @SuppressWarnings("hiding")
    private static Logger log = Logger.getLogger(RootApp.class);

    /**
     * Only call this when the webapp has been initialized!
     * 
     * @return true if we are in debug mode.
     */
    public static boolean isDebug() {
        final WebSite webSite = ThreadContext.getWebSite();
        if (webSite != null)
            return webSite.getProperty("debug") != null;
        return false;
    }
    
    private final List<InjectCommandFactory> factories;

    private final MenuWrapper menuwrapper;
    private ExceptionWrapper exceptionWrapper = new DefaultExceptionWrapper();

    public RootApp(List<InjectCommandFactory> factories, MenuWrapper menuwrapper) {
        this.factories = factories;
        this.menuwrapper = menuwrapper;
        this.localizer = Localizer.get();
    }

    @URLPattern("**")
    @Priority(MAX_PRIORITY)
    public Object blocked() {
        BlockingSystem w = BlockingSystem.getInstance();
        HttpServletRequest request = getServletRequest();
        String client = request.getHeader("X-Forwarded-For");
        if (client == null) {
            client = request.getRemoteHost() + ":" + request.getRemotePort();
        }

        if (w.isBlocked(client)) {
            log.warn(client + " is blocked.");
            return "Your host is blocked (" + client + ")";
        }
        return next();
    }

    /**
     * Catch all (non-jwig) exceptions happening while showing the page, and if
     * one happen send a blame pointer to the user pointing at the staff.
     * 
     * @throws Throwable
     */
    @URLPattern("**")
    @Priority(MAX_PRIORITY_BOOST)
    public Object renderExceptions() throws Throwable {
        try {
            return next();
        } catch (JWIGException e) {
            // Let JWIG handle its own exceptions...
            throw e;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                // We cannot use instanceof to check if the cause is a
                // ClientAbortException - so we do it in a bit ugly way...
                String causeName = cause.getClass().getName();
                if ("org.apache.catalina.connector.ClientAbortException"
                        .equals(causeName)) {
                    log.info("Connection reset by peer.");
                    return null;
                }
            }

            try {
                // Print error message to log
                if (!(e instanceof DoNotLogThisException)) {
                    try {
                        String requestUrl = ThreadContext.get().getRequestURL();
                        log.error(
                                "An error occurred while rendering the page: "
                                        + requestUrl, e);

                        ErrorLogger.get().log(e);
                    } catch (Throwable t) {
                        // ignore
                        // failing to notify ourselves should not annoy the
                        // user...
                    }
                }

                // Generate error
                Response error = ThreadContext.get().getResponse();
                int statusCode = 500;
                if (e instanceof HasStatusCode) {
                    statusCode = ((HasStatusCode) e).getStatusCode();
                }
                error.setStatus(statusCode);

                if (e instanceof Redirect) {
                    Redirect redirect = (Redirect) e;
                    return redirect.getRedirect();
                }

                // Generate error message
                XML xml;
                if (e instanceof Renderable)
                    xml = Localizer.get()
                            .localize(injectAboveCache(render((Renderable) e)),
                                    getLang());
                else {
                    xml = Localizer.get()
                            .localize(injectAboveCache(exceptionWrapper
                                            .getErrorMessage(e)), getLang());
                }

                // Send error message
                error.setXML(xml);
                return xml;
            } catch (Throwable t) {
                // this is really really bad, we even failed to render the error
                // message
                throw t;
            }
        }
    }

    protected abstract String getDefaultLang();

    /**
     * Method which should be 'partly' overriden by subclasses if they want to
     * inject stuff above the cache. Everything returned by this method is
     * injected above the cache.
     * 
     * By 'partly' overridden it is meant that super.getInjectCommands in almost
     * all cases should be included in the returned {@link InjectCommand}s.
     */
    protected List<InjectCommand> getInjectCommands() {
        final ArrayList<InjectCommand> commands = new ArrayList<>();
        return commands;
    }

    public MenuWrapper getMenuWrapper() {
        return menuwrapper;
    }

    /**
     * Access control method. Should determine if the current user has access to
     * the current page.
     * 
     * ex. only Admins should have access to an admin-app. Thus for everyone
     * else but the Admins, false should be returned.
     * 
     * @return whether the page is accessible or not.
     */
    protected abstract boolean hasAccess();

    @URLPattern("**")
    @Priority(MAX_PRIORITY)
    public Object improveMenus() {
        Object o = next();
        try {
            if (o instanceof XML) {
                XML xml = (XML) o;

                xml.set(new NamespaceDecl("fn",
                        "http://www.w3.org/2005/xpath-functions"));

                final String menuSelect = "//xhtml:div[@id='menu']";
                final String itemSelect = "//xhtml:ul/xhtml:li";
                final String menuItemSelect = menuSelect + itemSelect;
                final String menuLinkSelect = menuItemSelect
                        + "[./xhtml:a/@href]";

                final String requestURL = getRequestURL().toString();
                final URI requestURI = stringToURI(requestURL);
                
                int i = 1;
                for (Element menulink : xml.getElements(menuLinkSelect)) {
                    String href = menulink.getString("./xhtml:a/@href");
                    if (href != null) {
                        URI hrefURI = stringToURI(href);
                        if (hrefURI.equals(requestURI)) {
                            xml = xml.set(
                                    "(" + menuLinkSelect + ")[" + i + "]",
                                    new Attribute("class", "selected"));
                        }
                    }
                    i++;
                }

                // mark branches too
                xml = xml.set(menuItemSelect
                        + "[.//xhtml:ul and @class != 'selected']",
                        new Attribute("class", "branch"));
                xml = xml.set(menuItemSelect
                        + "[.//xhtml:ul and @class = 'selected']",
                        new Attribute("class", "branchselected selected"));
                xml = xml.set(menuItemSelect + "[.//xhtml:ul"
                        + "//xhtml:li[fn:contains(@class, 'selected')]]",
                        new Attribute("class", "branchselected"));
                return xml;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    private URI stringToURI(String url) throws URISyntaxException {
        final String requestURL = url.replace(' ', '+');
        final String decodedURL = URLEncoding.decode(requestURL);
        final String encodedURL = URLEncoding.encode(decodedURL);
        final String escapedURL = new URI(encodedURL).getPath();
        return new URI(escapedURL).normalize();
    }

    /**
     * Injects stuff above the cache. This is used for personalizing pages which
     * are cacheable. An example of such usage is injecting the name of the
     * currently logged in user, which can't be cached effectively.
     * 
     * @see InjectCommand
     * @see InjectCommandFactory
     */
    @URLPattern("**")
    @Priority(PRE_CACHE)
    public XML injectAboveCache() {
        Object o = next();
        if (o instanceof XML) {
            XML xml = (XML) o;
            return injectAboveCache(xml);
        }
        return null;
    }

    protected XML injectAboveCache(XML xml) {
        XML xmlVar = xml;
        final List<InjectCommand> injectCommands = new ArrayList<>(
                getInjectCommands());
        for (InjectCommandFactory factory : factories) {
            injectCommands.add(factory.create());
        }
        for (InjectCommand command : injectCommands) {
            final Object value = command.getValue();
            if (value != null)
                xmlVar = xmlVar.plug(command.getGapName(), value);
        }
        return xmlVar;
    }

    @URLPattern("**")
    @Priority(LOCALIZATION)
    public XML localize() {
        Object o = next();
        if (o instanceof XML) {
            XML xml = (XML) o;
            return localizer.localize(xml, getLang());
        }
        return null;
    }

    @Priority(REDIRECT_TO_SECURE)
    @URLPattern("**")
    public URL redirectToSecure() throws MalformedURLException {
        if (requiresSecure() && !isSecure()) {
            if (!isDebug()) {
                return new URL(getRequestURL().toString().replaceFirst("http",
                        "https"));
            }
        }
        next();
        return null;
    }

    @URLPattern("**")
    @Priority(Integer.MIN_VALUE)
    public URL removeTrailingSlash() throws MalformedURLException {
        String requestURL = getRequestURL().toString();
        if (requestURL.endsWith("/"))
            return new URL(requestURL.substring(0, requestURL.length() - 1));
        return null;
    }

    /**
     * Renders a page using the boost-tools
     * 
     * @param boostable
     *            as the boostable page to render
     * @return a rendered page
     */
    protected XML render(Renderable boostable) {
        return Renderer.render(boostable, menuwrapper);
    }

    protected boolean requiresSecure() {
        return true;
    }

    protected String selectLang() {
        // default:
        return getDefaultLang();
    }

    @Priority(LOCALIZATION - 1)
    @URLPattern("**")
    public Object setLang() {
        setLang(selectLang());
        return next();
    }

    protected void setLang(String l) {
        getResponse().setResponseScopeData(new RootAppResponseData(l));
    }

    protected void setExceptionWrapper(ExceptionWrapper exceptionWrapper) {
        this.exceptionWrapper = exceptionWrapper;
    }

    protected ExceptionWrapper getExceptionWrapper() {
        return exceptionWrapper;
    }

}

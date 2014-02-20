package dk.brics.jwig.boost;

import dk.brics.jwig.*;
import dk.brics.jwig.boost.exceptions.AccessDeniedException;
import dk.brics.jwig.boost.model.IUser;
import dk.brics.jwig.boost.model.IUserAuthenticationToken;
import dk.brics.jwig.boost.rendering.EALoginArea.AuthenticationTokenMaker;
import dk.brics.jwig.boost.rendering.EALoginArea.UserAuthenticator;
import dk.brics.jwig.boost.rendering.LoginExceptionFactory;
import dk.brics.jwig.boost.rendering.MenuWrapper;
import dk.brics.jwig.boost.rendering.hierarchy.Renderable;
import dk.brics.jwig.boost.rendering.injection.InjectCommandFactory;
import dk.brics.jwig.persistence.HibernateQuerier;
import dk.brics.xact.XML;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.Cookie;
import java.net.URL;
import java.util.List;

public abstract class UserApp extends RootApp {
    protected static final int AUTH = REDIRECT_TO_SECURE - 601; // 1400
    protected static final int COOKIELOGIN = AUTH + 1; // 1402
    protected static final int MANUAL_LOGIN_LOGOUT = AUTH + 2; // 1402
    protected static final int UUIDLOGIN = AUTH + 3; // 1403
    protected static final int ACCESSCONTROL = AUTH - 100; // 1300
    private static Logger log = Logger.getLogger(UserApp.class);
    private final boolean requiresLogin;
    private final UserAuthenticationTokenQuerier userAuthenticationTokenQuerier;
    private final UserQuerier userQuerier;
    private final AuthenticationTokenMaker tokenMaker;
    private final UserAuthenticator userAuthenticator;
    private LoginExceptionFactory loginExceptionFactory;

    public UserApp(List<InjectCommandFactory> factories,
                   MenuWrapper menuwrapper, boolean requiresLogin,
                   UserAuthenticationTokenQuerier userAuthenticationTokenQuerier,
                   UserQuerier userQuerier, AuthenticationTokenMaker tokenMaker,
                   UserAuthenticator userAuthenticator,
                   LoginExceptionFactory loginExceptionFactory) {
        super(factories, menuwrapper);
        this.requiresLogin = requiresLogin;
        this.userAuthenticationTokenQuerier = userAuthenticationTokenQuerier;
        this.userQuerier = userQuerier;
        this.tokenMaker = tokenMaker;
        this.userAuthenticator = userAuthenticator;
        this.loginExceptionFactory = loginExceptionFactory;
    }

    public static IUser getCurrentUser() {
        WebApp webApp = WebApp.get();
        if (webApp instanceof UserApp)
            return ((UserApp) webApp).getCurrentUser_non_static();
        throw new RuntimeException(
                "Implementation error: UserApp.getCurrentUser() called in a request without a UserApp subtype!");
    }

    private void setCurrentUser(IUser user) {
        if (user != null)
            log.info(String.format("User %s (%s) contacting the service", user.getLogin(), user.getName()));
        getResponse().setResponseScopeData(new UserResponseData(user));
    }

    public static IUser getCurrentUser(UserQuerier userQuerier) {
        WebApp webApp = WebApp.get();
        if (webApp instanceof UserApp)
            return ((UserApp) webApp).getCurrentUser_non_static();
        throw new RuntimeException(
                "Implementation error: UserApp.getCurrentUser() called in a request without a UserApp subtype!");
    }

    private IUser getCurrentUser_non_static() {
       if (getResponse().hasResponseScopeData(UserResponseData.class)) {
            return getResponse().getResponseScopeData(UserResponseData.class).user;
        }
        return null;
    }

    /**
     * Filter which checks whether the current user has access to the current
     * pageor not. This is defined by the {@link #hasAccess()} method.
     *
     * @see #hasAccess()
     */
    @URLPattern("**")
    @Priority(ACCESSCONTROL)
    public void accessControl() {
        if (!hasAccess()) {
            throw new AccessDeniedException(
                    getAccessDeniedExplanation());
        }
        next();
    }

    /**
     * Default implementation of the access denied explanation displayed to the user upon a failed #accessControl / #hasAccess invocation
     */
    protected XML getAccessDeniedExplanation() {
        return XML.parseTemplate("<[s_Accessdeniedexplanation]>");
    }

    @URLPattern("**")
    @Priority(AUTH)
    public void auth() {
        if (getCurrentUser_non_static() == null && isRequiresLogin())
            forceLogin();
        next();
    }

    protected void forceLogin(URL returnURL) {
        if (getCurrentUser_non_static() == null)
            throw loginExceptionFactory.loginRequired(returnURL);
    }

    protected void forceLogin() {
        forceLogin(getRequestURL());
    }

    @URLPattern("**")
    @Priority(COOKIELOGIN)
    public void cookieLogin() {
        // abort if already logged in otherwise
        if (getCurrentUser_non_static() == null) {
            Cookie cookie = CookieUtil
                    .getCookie(IUserAuthenticationToken.COOKIENAME);
            if (cookie != null) {
                String value = cookie.getValue();
                if (!value.isEmpty()) {
                    IUserAuthenticationToken token = userAuthenticationTokenQuerier
                            .getByCookieValue(value);
                    if (token != null)
                        setCurrentUser(token.getUser());
                }
            }
        }
        next();
    }

    public final boolean isRequiresLogin() {
        return requiresLogin;
    }

    public abstract Class<? extends IUser> retype(IUser user);

    @URLPattern("**")
    @Priority(UUIDLOGIN)
    public Object uuidLogin(@ParamName("guid") String uuid) {
        if (uuid != null) {
            IUser userByUUID = userQuerier.getByUUID(uuid);
            if (userByUUID != null) {
                setCurrentUser(userByUUID);
            }
        }
        return next();
    }

    @Priority(MANUAL_LOGIN_LOGOUT)
    @URLPattern("logout")
    public URL logout(URL returnURL) {
        setCurrentUser(null);
        Cookie cookie = CookieUtil
                .getCookie(IUserAuthenticationToken.COOKIENAME);
        if (cookie != null) {
            cookie.setPath("/");
            cookie.setValue("");
            cookie.setMaxAge(0);
            WebContext.getServletResponse().addCookie(cookie);
        }
        return TextUtil.uniqifyURL(returnURL);
    }

    @GET
    @URLPattern("passwordLogin")
    public XML passwordLoginGet() {
        return render(new Renderable() {
            //
        });
    }

    @GET
    @URLPattern("logout")
    public XML logoutGet() {
        return render(new Renderable() {
            //
        });
    }

    @POST
    @Priority(MANUAL_LOGIN_LOGOUT)
    @URLPattern("passwordLogin")
    public URL passwordLogin(@RequiredParameter String login,
                             @RequiredParameter String password, boolean rememberMe,
                             @RequiredParameter URL returnURL) {
        HibernateQuerier hq = new HibernateQuerier();
        Session session = hq.getSession();
        IUser user = userQuerier.getUserByLogin(login);
        if (user != null) {
            if (userAuthenticator.authenticateUser(user, password)) {
                Transaction transaction = hq.getOrBeginTransaction();
                log.info("User " + user.getLogin() + " has logged in");
                IUserAuthenticationToken userAuthenticationToken = tokenMaker
                        .make(user);
                session.save(userAuthenticationToken);
                final Cookie cookie = new Cookie(
                        IUserAuthenticationToken.COOKIENAME,
                        userAuthenticationToken.getUuid());
                cookie.setPath("/");
                if (rememberMe)
                    cookie.setMaxAge(31 * 24 * 60 * 60); // a month
                else
                    cookie.setMaxAge(-1); // session
                // the login cookie has been set, let cookie-login log the user
                // in
                WebContext.getServletResponse().addCookie(cookie);
                transaction.commit();
            } else {
                log.info(String.format("Failed to authenticate %s. User exists but password is wrong", login));
                if (userQuerier.getUserByLogin(login) == null) {
                } else {
                }
                throw loginExceptionFactory.invalidPassword(returnURL);
            }
        } else {
            log.info(String.format("Failed to authenticate %s. No such user", login));
            throw loginExceptionFactory.invalidUsername(returnURL);
        }
        return TextUtil.uniqifyURL(returnURL);
    }

    public static interface UserAuthenticationTokenQuerier {
        IUserAuthenticationToken getByCookieValue(String value);
    }

    public static interface UserQuerier {
        IUser getByUUID(String uuid);

        IUser getUserByLogin(String login);

        IUser getUserById(String string);
    }

    public static class CookieUtil {
        public static Cookie getCookie(String name) {
            Cookie[] cookies = getServletRequest().getCookies();
            if (cookies == null) {
                return null;
            }

            for (Cookie c : cookies) {
                final String cname = c.getName();
                if (name.equals(cname))
                    return c;
            }
            return null;
        }
    }

    private class UserResponseData {
        IUser user;

        private UserResponseData(IUser user) {
            this.user = user;
        }
    }
}

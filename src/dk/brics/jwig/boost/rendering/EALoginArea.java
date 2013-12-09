package dk.brics.jwig.boost.rendering;

import java.net.URL;

import dk.brics.jwig.WebApp;
import dk.brics.jwig.WebContext;
import dk.brics.jwig.boost.UserApp;
import dk.brics.jwig.boost.model.IUser;
import dk.brics.jwig.boost.model.IUserAuthenticationToken;
import dk.brics.jwig.boost.rendering.hierarchy.Renderer;
import dk.brics.jwig.boost.rendering.hierarchy.Standalone;
import dk.brics.jwig.boost.rendering.hierarchy.URLHandlerFormContent;
import dk.brics.jwig.boost.rendering.injection.InjectCommand;
import dk.brics.jwig.boost.rendering.injection.InjectCommandFactory;
import dk.brics.jwig.boost.rendering.uicomponents.PlaceholderAttribute;
import dk.brics.jwig.boost.rendering.uicomponents.inputtag.CheckBoxInputTag;
import dk.brics.jwig.boost.rendering.uicomponents.inputtag.PasswordInputTag;
import dk.brics.jwig.boost.rendering.uicomponents.inputtag.TextInputTag;
import dk.brics.xact.ToXMLable;
import dk.brics.xact.XML;

public class EALoginArea implements InjectCommandFactory {
    public interface AuthenticationTokenMaker {
        IUserAuthenticationToken make(IUser user);
    }

    public class LoginArea implements ToXMLable {
        private final boolean useRememberMe;

        public LoginArea(boolean useRememberMe) {
            this.useRememberMe = useRememberMe;
        }

        private XML makeRememberMe() {
            final XML checkBox = new CheckBoxInputTag("rememberMe", false)
                    .toXML();
            return XML.parseTemplate("<[CHECKBOX]> <[s_Remember_me]>").plug(
                    "CHECKBOX", checkBox);
        }

        @Override
        public XML toXML() {
            XML xml = XML
                    .parseTemplate(
                            "<div class='loginarea'><div><[LOGIN]></div><div><[PASSWORD]></div><div><[REMEMBERME]></div><input type='submit' name='doLogin' value=[s_Login]/></div><input type='hidden' name='returnURL' value=[URL]/>")
                    .plug("LOGIN",
                            new TextInputTag("login", "",
                                    new PlaceholderAttribute("Login")))
                    .plug("PASSWORD",
                            new PasswordInputTag("password", "",
                                    new PlaceholderAttribute("Password")));
            if (useRememberMe)
                xml = xml.plug("REMEMBERME", makeRememberMe());
            xml = xml.plug("URL", WebContext.getRequestURL());
            return xml;
        }
    }

    public class LoginAreaForm implements Standalone, URLHandlerFormContent {

        @Override
        public XML getContent() {
            return new LoginArea(true).toXML();
        }

        @Override
        public URL getHandler() {
            // send the request to a special post-webmethod which resides above
            // the security filters
            final Class<? extends WebApp> currentWebAppClass = getCurrentWebApp();
            return WebContext
                    .makeURL(true, currentWebAppClass, "passwordLogin");
        }

    }

    public class LogoutAreaForm implements URLHandlerFormContent, Standalone {

        private final IUser user;

        public LogoutAreaForm(IUser user) {
            this.user = user;
        }

        @Override
        public XML getContent() {
            return XML
                    .parseTemplate(
                            "<div class='loginarea'><div><[s_Logged_in_as]>:</div><div><[NAME]></div><div><input type='submit' name='logout' value=[s_Logout]/></div></div><input type='hidden' name='returnURL' value=[URL]/>")
                    .plug("NAME", user.getName())
                    .plug("URL", WebContext.getRequestURL());
        }

        @Override
        public URL getHandler() {
            final Class<? extends WebApp> currentWebAppClass = getCurrentWebApp();
            return WebContext.makeURL(true, currentWebAppClass, "logout");
        }

    }

    public interface UserAuthenticator {

        boolean authenticateUser(IUser user, String password);

    }

    public interface UserQuerier {
        IUser getUserByLogin(String login);
    }

    private Class<? extends WebApp> getCurrentWebApp() {
        return WebApp.get().getClass();
    }

    @Override
    public InjectCommand create() {
        return new InjectCommand() {
            @Override
            public String getGapName() {
                return "INJECT_BOOST_LOGINAREA";
            }

            @Override
            public XML getValue() {
                IUser user = UserApp.getCurrentUser();
                if (user == null)
                    return Renderer.render(new LoginAreaForm());
                return Renderer.render(new LogoutAreaForm(user));
            }
        };
    }
}

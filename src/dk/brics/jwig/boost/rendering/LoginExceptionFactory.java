package dk.brics.jwig.boost.rendering;

import java.net.URL;

public interface LoginExceptionFactory {
    RuntimeException loginRequired(URL returnURL);

    RuntimeException invalidUsername(URL returnURL);

    RuntimeException invalidPassword(URL returnURL);
}

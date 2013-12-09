package dk.brics.jwig.boost.rendering;

import java.net.URL;

import dk.brics.jwig.AccessDeniedException;
import dk.brics.jwig.boost.exceptions.EALoginRequiredException;

public class EALoginExceptionFactory implements LoginExceptionFactory {

    @Override
    public RuntimeException loginRequired(URL returnURL) {
        return new EALoginRequiredException();
    }

    @Override
    public RuntimeException invalidUsername(URL returnURL) {
        return new AccessDeniedException();
    }
    
    @Override
    public RuntimeException invalidPassword(URL returnURL) {
        return new AccessDeniedException();
    }

}

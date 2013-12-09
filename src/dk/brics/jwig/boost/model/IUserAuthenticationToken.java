package dk.brics.jwig.boost.model;

import dk.brics.jwig.persistence.Persistable;

public interface IUserAuthenticationToken extends Persistable {
    public static final String COOKIENAME = "IUserAuthenticationToken";

    IUser getUser();

    String getUuid();
}

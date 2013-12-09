package dk.brics.jwig.boost.model;

import java.util.Set;

import dk.brics.jwig.persistence.Persistable;

public interface IUser extends Persistable {
    @Override
    public Integer getId();

    /**
     * The preffered language of the user. Ex.: "en" or "da", for english and
     * danish respectively.
     */
    public String getLang();

    /**
     * The name the user uses to log into the system - this should be unique for
     * all users. Ex.: "johnd"
     */
    public String getLogin();

    /**
     * The real-life name of the user. Ex.: "John Doe"
     */
    public String getName();

    /**
     * The access rights of the users. A user can have multiple roles, each
     * giving different access rights, at the same time.
     */
    public Set<? extends IUserRole> getIUserRoles();

    public String getUuid();

    public void setLang(String lang);

    public void setLogin(String login);

    public void setName(String name);
}

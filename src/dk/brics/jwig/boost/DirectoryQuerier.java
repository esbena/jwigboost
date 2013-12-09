package dk.brics.jwig.boost;

/**
 * Queries various user data from a directory server. Any method except {@link #validateUser(String, String)}
 * may return the empty string to indicate no available data
 *
 * @author schwarz
 * @created 2009-02-02 16:16:39
 */
public interface DirectoryQuerier {

    /**
     * Return true iff the credentials are valid
     * @param username
     * @param passwd
     * @return
     */
    boolean validateUser(String username, String passwd);

    String getEmailFromUsername(String username);

    String getUsernameFromStudentId(String id);

    String getStudentIdFromUsername(String login);

    String getRealNameFromDistinctUsername(String username);

    String getDepartmentAbbrevFromUsername(String username);

    String getDepartmentNameFromUsername(String username);

    String getUsernameFromEmail(String email);

    String getAttrFromUsername(String username, String attrName);
}

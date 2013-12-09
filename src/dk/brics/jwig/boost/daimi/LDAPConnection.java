package dk.brics.jwig.boost.daimi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import dk.brics.jwig.boost.DirectoryQuerier;

/**
 * A class that can retrieve various user data from the daimi ldap servers.
 * Connections are reused to send multiple queries. If more than 5 minutes have
 * passed since last query, the connection will be closed and a new one is
 * opened for the next query.
 * 
 * @author: schwarz
 * @created: 04-12-2007 15:22:01
 */
public class LDAPConnection implements DirectoryQuerier {
    private static Logger logger = Logger.getLogger(LDAPConnection.class);
    private static final String servername = "ldap://ds.nfit.au.dk/";
    private volatile DirContext context;

    private final boolean isDebug;

    public LDAPConnection(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Override
    public synchronized boolean validateUser(String username, String passwd) {
        // when testing, all logins are valid!
        if (isDebug) {
            return true;
        }
        String usernameVar = username;
        String passwdVar = passwd;
        try {
            if (usernameVar.equals("")) {
                usernameVar = "unknown";
            }
            if (passwdVar.equals("")) {
                passwdVar = "none";
            }
            long l = System.currentTimeMillis();
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, servername);

            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, "uid=" + escapeDN(usernameVar)
                    + ",ou=people,o=auud");
            env.put(Context.SECURITY_CREDENTIALS, passwdVar);
            // This constructor throws an exception if user validation fails
            @SuppressWarnings("unused")
            InitialDirContext uservalidation = new InitialDirContext(env);
            logger.info("It took " + (System.currentTimeMillis() - l)
                    + " ms to validate user " + usernameVar);
            return true;
        } catch (NamingException e) {
            return false;
        }
    }

    @Override
    public String getAttrFromUsername(String username, String attrName) {
        try {
            DirContext ctx = getAnonymousBindEnv();
            Attributes a = new BasicAttributes(true);
            a.put("uid", username);
            NamingEnumeration<SearchResult> namingEnumeration = ctx.search(
                    "ou=people,o=auud", a);
            if (!namingEnumeration.hasMore()) {
                ctx = getAnonymousBindEnv();
                a = new BasicAttributes(true);
                a.put("diUsername", username);
                namingEnumeration = ctx.search("ou=people,o=auud", a);
                if (!namingEnumeration.hasMore()) {
                    return "";
                }
            }
            SearchResult searchResult = namingEnumeration.next();
            Attribute mail = searchResult.getAttributes().get(attrName);
            Object mailS = mail.get();
            return mailS.toString();
        } catch (Exception e) {
            logger.debug(e);
            return "";
        }
    }

    @Override
    public synchronized String getUsernameFromStudentId(String id) {
        String attr = "uid";
        try {
            DirContext ctx = getAnonymousBindEnv();
            Attributes a = new BasicAttributes(true);
            a.put("auStudentID", id);
            NamingEnumeration<SearchResult> namingEnumeration = ctx.search(
                    "ou=people,o=auud", a);
            SearchResult searchResult = namingEnumeration.next();
            Attribute found = searchResult.getAttributes().get(attr);
            NamingEnumeration<?> namingEnumeration1 = found.getAll();
            if (found.size() != 1) {
                return "";
            }
            Object value = namingEnumeration1.next();
            return value.toString();
        } catch (Exception e) {
            logger.debug(e, e);
            return "";
        }
    }

    @Override
    public synchronized String getUsernameFromEmail(String email) {
        String attr = "uid";
        try {
            DirContext ctx = getAnonymousBindEnv();
            Attributes a = new BasicAttributes(true);
            a.put("mail", email);
            NamingEnumeration<SearchResult> namingEnumeration = ctx.search(
                    "ou=people,o=auud", a);
            SearchResult searchResult = namingEnumeration.next();
            Attribute found = searchResult.getAttributes().get(attr);
            NamingEnumeration<?> namingEnumeration1 = found.getAll();
            if (found.size() != 1) {
                return "";
            }
            Object value = namingEnumeration1.next();
            return value.toString();
        } catch (Exception e) {
            logger.debug(e, e);
            return "";
        }
    }

    @Override
    public synchronized String getStudentIdFromUsername(String username) {
        String attr = "auStudentID";
        return getAttrFromUsername(username,attr);
    }

    @Override
    public synchronized String getRealNameFromDistinctUsername(String username) {
        String attr = "cn";
        return getAttrFromUsername(username, attr);
    }

    @Override
    public synchronized String getDepartmentAbbrevFromUsername(String username) {
        String attr = "ou";
        return getAttrFromUsername(username, attr);
    }

    @Override
    public synchronized String getDepartmentNameFromUsername(String username) {
        String attr = "o";
        return getAttrFromUsername(username, attr);
    }

    @Override
    public synchronized String getEmailFromUsername(String username) {
        String attr = "mail";
        return getAttrFromUsername(username, attr);
    }


    /**
     * Returns (and possibly creates) a connection to the LDAP server
     */
    private DirContext getAnonymousBindEnv() throws NamingException {
        if (context == null) {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, servername);
            context = new InitialDirContext(env);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000 * 60 * 5); // Renew connection after 5
                        // minutes.
                    } catch (InterruptedException e) {
                        Logger.getLogger(LDAPConnection.class).warn(e, e);
                    }
                    context = null;
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        return context;
    }

    /**
     * From:
     * @see <href="http://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java">OWASP: Preventing LDAP Injection in Java</a>
     */
    public static String escapeDN(String name) {
       StringBuilder sb = new StringBuilder();
       if ((name.length() > 0) && ((name.charAt(0) == ' ') || (name.charAt(0) == '#'))) {
           sb.append('\\'); // add the leading backslash if needed
       }
       for (int i = 0; i < name.length(); i++) {
           char curChar = name.charAt(i);
           switch (curChar) {
               case '\\':
                   sb.append("\\\\");
                   break;
               case ',':
                   sb.append("\\,");
                   break;
               case '+':
                   sb.append("\\+");
                   break;
               case '"':
                   sb.append("\\\"");
                   break;
               case '<':
                   sb.append("\\<");
                   break;
               case '>':
                   sb.append("\\>");
                   break;
               case ';':
                   sb.append("\\;");
                   break;
               default:
                   sb.append(curChar);
           }
       }
       if ((name.length() > 1) && (name.charAt(name.length() - 1) == ' ')) {
           sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
       }
       return sb.toString();
   }

    public static void main(String[] args) throws NamingException {
        LDAPConnection con = new LDAPConnection(false);
        BasicConfigurator.configure();
        args[0] = con.getUsernameFromStudentId(args[0]);
        System.out.println((args[0]));
        System.out.println(con.getRealNameFromDistinctUsername(args[0]));
        System.out.println(con.getEmailFromUsername(args[0]));
        System.out.println(con.getStudentIdFromUsername(args[0]));
        con.context.close();
        con.context = null;
    }
}
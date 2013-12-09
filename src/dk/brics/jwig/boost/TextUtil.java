package dk.brics.jwig.boost;

import dk.brics.jwig.BadRequestException;
import dk.brics.jwig.WebContext;
import dk.brics.jwig.persistence.Persistable;
import dk.brics.jwig.server.ThreadContext;
import dk.brics.xact.XML;
import org.hibernate.Session;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author schwarz
 * @created 2007-03-01 09:45:49
 */
public class TextUtil {

    private static Map<String, Object> emptyMap;

    /**
     * Tries to parse a date string using one of the supplied formats.
     */
    public static Calendar parseDate(String dateString, String... formats)
            throws ParseException {
        for (String format : formats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format);
                Date date = parser.parse(dateString);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal;
            } catch (ParseException e) {
                continue; // try next format
            }
        }
        throw new ParseException(dateString
                + " did not match any of the formats: "
                + Arrays.toString(formats), 0);

    }

    public static Calendar parseDate(String dateString, Calendar defaultDate,
                                     String... formats) {
        try {
            return parseDate(dateString, formats);
        } catch (ParseException e) {
            return defaultDate;
        }
    }

    /**
     * Creates a name, as close to the desired name as possible, without being
     * the same as an existing name.
     */
    public static String getUniqueName(String desired, Set<String> existing) {
        if (!existing.contains(desired))
            return desired;
        for (int i = 1; true; i++) {
            final String suggested = desired + "-" + i;
            if (!existing.contains(suggested)) {
                return suggested;
            }
        }
    }

    public static String fix(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() == 0) {
            return "\u00a0";
        }
        return s;
    }

    public static XML getMailToAsLink(String addresses, String text, boolean encrypt) {
        return getMailToAsLink(addresses,
                XML.parseTemplate("<[TEXT]>").plug("TEXT", text), encrypt);
    }

    public static XML makeExternalLink(Object content, URL url) {
        return makeLink(content, url, "external");
    }

    public static XML makeLink(Object content, URL url) {
        return makeLink(content, url, null);
    }

    private static XML makeLink(Object content, URL url, String classs) {
        XML xml = XML
                .parseTemplate("<a href=[URL] class=[CLASS]><[CONTENT]></a>");
        xml = xml.plug("URL", url);
        xml = xml.plug("CONTENT", content);
        if (classs != null)
            xml = xml.plug("CLASS", classs);
        return xml;

    }

    public static Map<String, Object> noWebAppArgs() {
        if (emptyMap == null)
            emptyMap = Collections
                    .unmodifiableMap(new HashMap<String, Object>());
        return emptyMap;
    }

    public static XML getMailToAsLink(String addresses, XML text, boolean encrypt) {
        return XML.parseTemplate("<a href=[HREF]><[TEXT]></a>")
                .plug("HREF", encrypt? encryptMailto(addresses): "mailto:" + addresses).plug("TEXT", text);
    }

    /**
     * Naive encryption for email obfuscation: chars to char-values, separated
     * by '-'
     * <p/>
     * ex.: "foo" -> "102-111-111"
     */
    private static String encryptMailto(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            sb.append((int) (string.charAt(i))
                    + (i == string.length() - 1 ? "" : "-"));
        }
        return "javascript:jwigboost.decryptMailto('" + sb.toString() + "')";
    }

    public static String abbrev(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 3) + "...";
    }

    public static String nbsp(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ' ') {
                b.append("\u00a0");
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Safe int parsing, with default 0
     */
    public static int safeParseInt(String s) {
        if (s == null)
            return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String printDate(Calendar g) {
        GregorianCalendar today = new GregorianCalendar();
        if (g.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && g.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && g.get(Calendar.DAY_OF_MONTH) == today
                .get(Calendar.DAY_OF_MONTH)) {
            return new SimpleDateFormat("HH:mm").format(g.getTime());
        }
        return new SimpleDateFormat(RootApp.getDateFormat())
                .format(g.getTime());
    }

    public static XML printWeekDay(Calendar date) {
        XML weekday;
        switch (date.get(java.util.Calendar.DAY_OF_WEEK)) {
            case java.util.Calendar.MONDAY:
                weekday = XML.parseTemplate("<[s_Monday]>");
                break;
            case java.util.Calendar.TUESDAY:
                weekday = XML.parseTemplate("<[s_Tuesday]>");
                break;
            case java.util.Calendar.WEDNESDAY:
                weekday = XML.parseTemplate("<[s_Wednesday]>");
                break;
            case java.util.Calendar.THURSDAY:
                weekday = XML.parseTemplate("<[s_Thursday]>");
                break;
            case java.util.Calendar.FRIDAY:
                weekday = XML.parseTemplate("<[s_Friday]>");
                break;
            case java.util.Calendar.SATURDAY:
                weekday = XML.parseTemplate("<[s_Saturday]>");
                break;
            case java.util.Calendar.SUNDAY:
                weekday = XML.parseTemplate("<[s_Sunday]>");
                break;
            default:
                weekday = XML.parseTemplate("<b>No such day</b>");
                break;
        }
        return weekday;
    }

    public static String readFileAsString(File file) throws java.io.IOException {
        return readStreamAsString(new FileInputStream(file));
    }

    public static String readFileAsString(File file, String encoding) throws java.io.IOException {
        return readStreamAsString(new FileInputStream(file), encoding);
    }

    public static boolean isURLable(String potentialURL) {
        final boolean triviallyNot = potentialURL == null
                || potentialURL.equals("");
        if (triviallyNot)
            return false;
        try {
            @SuppressWarnings("unused")
            URL dummy = new URL(potentialURL);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    public static String readStreamAsString(InputStream is) throws IOException {
        return readStreamAsString(is,"UTF-8");
    }

    public static String readStreamAsString(InputStream is, String encoding)
            throws java.io.IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, encoding));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * To be used by AbstractHandlers receiving submit messages of the form
     * 'message:value', where the value corresponds to the id of some object of
     * type 'clazz' in the database.
     */
    public static <E extends Persistable> E getFromArg(Class<E> clazz,
                                                       String submit, Session session) {
        String[] parts = submit.split(":");
        if (parts.length < 2) {
            throw new BadRequestException(
                    "Invalid argument supplied. No ID specified.");
        }

        try {
            int value = Integer.parseInt(submit.split(":")[1]);
            E object = ThreadContext.getWebSite().getQuerier()
                    .getObject(clazz, value);
            return object;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Failed to parse ID.", e);
        }
    }

    /**
     * Creates a name, as close to the desired name as possible, without being
     * the same as an existing name. The extensions of the file is preserved.
     */
    public static String getUniqueFileName(String desired, Set<String> existing) {
        if (!existing.contains(desired))
            return desired;

        int extensionStartPosition = desired.indexOf(".");
        if (extensionStartPosition == -1)
            extensionStartPosition = desired.length();
        final String originalNamePrefix = desired.substring(0,
                extensionStartPosition);
        final String originalNameSuffix = desired
                .substring(extensionStartPosition);
        for (int i = 1; true; i++) {
            final String suggested = originalNamePrefix + "-" + i
                    + originalNameSuffix;
            if (!existing.contains(suggested)) {
                return suggested;
            }
        }
    }

    /**
     * Adds a unique (most likely at least) suffix to an URL. Used to make sure
     * the browser does not use a cached version of the page.
     *
     * @param originalURL as the URL to put a suffix on
     * @return the URL with a unique suffix
     */
    public static URL uniqifyURL(URL originalURL) {
        try {
            // ensure the browser does not display a locally cached version:
            final String suffixAppendSymbol = (originalURL.getQuery() == null) ? "?"
                    : "&";
            final String uniqueSuffix = suffixAppendSymbol + "ie="
                    + new GregorianCalendar().getTimeInMillis();
            String uniqueURLString = originalURL.toString() + uniqueSuffix;
            return new URL(WebContext.getServletResponse().encodeRedirectURL(
                    uniqueURLString));
        } catch (MalformedURLException e) {
            return originalURL;
        }

    }

    /**
     * Removes invalid characters in XML attribute values, according to
     * <tt>(<a href="http://www.w3.org/TR/REC-xml/#NT-Char" target="_top">Char</a>)*</tt>.
     *
     * @param text The text to be sanitize
     * @return Sanitized string containing only valid XML attribute values.
     */
    public static String sanitizeAttributeValue(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            boolean add = true;
            char c = text.charAt(i);
            if ('\uD800' <= c && c <= '\uDBFF') {
                if (i + 1 == text.length())
                    add = false;
                char d = text.charAt(i + 1);
                if (!('\uDC00' <= d && d <= '\uDFFF'))
                    add = false;
            }
            if (" \t\n\r".indexOf(c) == -1
                    && !('\u0020' <= c && c <= '\uD7FF')
                    && !('\ue000' <= c && c <= '\ufffd')
                    && c != ' ' && c != '\t' && c != '\r' && c != '\n')
                add = false;
            if (add)
                builder.append(c);
        }
        return builder.toString();
    }

}

package dk.brics.jwig.boost;

import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import dk.brics.xact.XML;

public class Localizer {
    private static final Logger log = Logger.getLogger(Localizer.class);
    private static Localizer instance;
    private final Map<String, PropertyResourceBundle> bundles;
    private String defaultLang;

    private Localizer() {
        bundles = new HashMap<>();
        // singleton
    }

    public void putBundle(String lang, PropertyResourceBundle bundle) {
        bundles.put(lang, bundle);
    }

    public String localize(String string, String lang) {
        return getBundleForLang(lang).getString(string);
    }

    public XML localize(XML x, String lang) {
        log.debug("Localizing...");
        long l = System.currentTimeMillis();
        ResourceBundle bundle = getBundleForLang(lang);
        XML xml = x;
        for (String key : bundle.keySet()) {
            String value = bundle.getString(key);
            xml = xml.plug("s_" + key, XML.parseTemplate(value));
            xml = xml.plug("sa_" + key, value);
        }
        log.debug("Localizing done in " + (System.currentTimeMillis() - l)
                + " ms");
        return xml;
    }

    /**
     * Returns the bundle for the given language. If not language is found the
     * default language is used.
     * 
     * @param lang
     *            The language which specifies the bundle
     * @return The found bundle, or the bundle for the default language, if no
     *         bundle is found.
     */
    private ResourceBundle getBundleForLang(String lang) {
        ResourceBundle bundle;
        if (!bundles.containsKey(lang)) {
            bundle = bundles.get(defaultLang);
        } else {
            bundle = bundles.get(lang);
        }
        return bundle;
    }

    public static Localizer get() {
        if (instance == null)
            instance = new Localizer();
        return instance;
    }

    public void setDefaultLanguage(String lang) {
        this.defaultLang = lang;
    }

    public String getDefaultLang() {
        return defaultLang;
    }
}

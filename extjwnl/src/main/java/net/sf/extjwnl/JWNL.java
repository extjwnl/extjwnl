package net.sf.extjwnl;

import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.ResourceBundleSet;

import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contains system info and JWNL properties.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class JWNL {
    private static final String CORE_RESOURCE = "JWNLResource";

    private static ResourceBundleSet bundle;

    private static boolean initialized = false;

    static {
        initialize();
    }

    public static void initialize() {
        if (!initialized) {
            bundle = new ResourceBundleSet(CORE_RESOURCE);
            // set the locale
            //bundle.setLocale(Locale.getDefault());//enable when enough translations will be available.
            bundle.setLocale(new Locale("en", ""));

            initialized = true;
        }
    }

    /**
     * Create a private JWNL to prevent construction.
     */
    private JWNL() {
    }

    /**
     * Parses a properties file and sets the ready state at various points. Initializes the
     * various PointerType, Adjective, and VerbFrame necessary preprocessing items.
     *
     * @param propertiesStream the properties file stream
     * @throws JWNLException various JWNL exceptions, depending on where this fails
     */
    public static void initialize(InputStream propertiesStream) throws JWNLException {
        Dictionary.setInstance(Dictionary.getInstance(propertiesStream));
    }

    public static ResourceBundle getResourceBundle() {
        return bundle;
    }

    public static ResourceBundleSet getResourceBundleSet() {
        return bundle;
    }

    /**
     * Resolves <var>msg</var> in one of the resource bundles used by the system
     *
     * @param msg message to resolve
     * @return resolved message
     */
    public static String resolveMessage(String msg) {
        return resolveMessage(msg, new Object[0]);
    }

    /**
     * Resolve <var>msg</var> in one of the resource bundles used by the system.
     *
     * @param msg message to resolve
     * @param obj parameter to insert into the resolved message
     * @return resolved message
     */
    public static String resolveMessage(String msg, Object obj) {
        return resolveMessage(msg, new Object[]{obj});
    }

    /**
     * Resolve <var>msg</var> in one of the resource bundles used by the system
     *
     * @param msg    message to resolve
     * @param params parameters to insert into the resolved message
     * @return resolved message
     */
    public static String resolveMessage(String msg, Object[] params) {
        return insertParams(bundle.getString(msg), params);
    }

    private static String insertParams(String str, Object[] params) {
        StringBuilder buf = new StringBuilder();
        int startIndex = 0;
        for (int i = 0; i < params.length && startIndex <= str.length(); i++) {
            int endIndex = str.indexOf("{" + i, startIndex);
            if (endIndex != -1) {
                buf.append(str.substring(startIndex, endIndex));
                buf.append(params[i] == null ? null : params[i].toString());
                startIndex = endIndex + 3;
            }
        }
        buf.append(str.substring(startIndex, str.length()));
        return buf.toString();
    }
}
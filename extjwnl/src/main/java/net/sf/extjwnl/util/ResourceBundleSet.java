package net.sf.extjwnl.util;

import java.util.*;

/**
 * A ResourceBundle that is a proxy to multiple ResourceBundles.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ResourceBundleSet extends ResourceBundle {

    private Locale locale = null;
    private final List<String> resources = new ArrayList<>();

    public ResourceBundleSet(String resource) {
        addResource(resource);
    }

    public void addResource(String resource) {
        resources.add(resource);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Enumeration<String> getKeys() {
        return new Enumeration<String>() {
            private final Iterator<String> itr = resources.iterator();
            private Enumeration<String> currentEnum;

            public boolean hasMoreElements() {
                if (currentEnum == null || !currentEnum.hasMoreElements()) {
                    if (itr.hasNext()) {
                        currentEnum = getBndl(itr.next()).getKeys();
                    }
                }
                return currentEnum != null && currentEnum.hasMoreElements();
            }


            public String nextElement() {
                return currentEnum.nextElement();
            }
        };
    }

    /**
     * Resolves <var>msg</var>.
     *
     * @param msg message to resolve
     * @return resolved message
     */
    public String resolveMessage(String msg) {
        return resolveMessage(msg, new Object[0]);
    }

    /**
     * Resolves <var>msg</var>.
     *
     * @param msg message to resolve
     * @param obj parameter to insert into the resolved message
     * @return resolved message
     */
    public String resolveMessage(String msg, Object obj) {
        return resolveMessage(msg, new Object[]{obj});
    }

    /**
     * Resolves <var>msg</var>
     *
     * @param msg    message to resolve
     * @param params parameters to insert into the resolved message
     * @return resolved message
     */
    public String resolveMessage(String msg, Object[] params) {
        return insertParams(getString(msg), params);
    }

    public static String insertParams(String str, Object[] params) {
        StringBuilder buf = new StringBuilder();
        int startIndex = 0;
        for (int i = 0; i < params.length && startIndex <= str.length(); i++) {
            int endIndex = str.indexOf("{" + i, startIndex);
            if (endIndex != -1) {
                buf.append(str, startIndex, endIndex);
                buf.append(params[i] == null ? null : params[i].toString());
                startIndex = endIndex + 3;
            }
        }
        buf.append(str.substring(startIndex));
        return buf.toString();
    }

    protected Object handleGetObject(String key) {
        for (String resource : resources) {
            ResourceBundle bundle = getBndl(resource);
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        return key;
    }

    private ResourceBundle getBndl(String bundle) {
        if (null != locale) {
            return ResourceBundle.getBundle(bundle, locale);
        } else {
            return ResourceBundle.getBundle(bundle);
        }
    }
}
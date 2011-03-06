package net.sf.extjwnl.util;

import java.util.*;

/**
 * A ResourceBundle that is a proxy to multiple ResourceBundles.
 */
public class ResourceBundleSet extends ResourceBundle {

    private static final MessageLog log = new MessageLog(ResourceBundleSet.class);

    private Locale locale = Locale.getDefault();
    private Set<String> resources = new HashSet<String>();

    public ResourceBundleSet(String resource) {
        addResource(resource);
    }

    public ResourceBundleSet(String[] resources) {
        for (String resource : resources) {
            addResource(resource);
        }
    }

    public void addResource(String resource) {
        resources.add(resource);
    }

    public String[] getResources() {
        return resources.toArray(new String[resources.size()]);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    protected Object handleGetObject(String key) {
        for (String resource : resources) {
            try {
                ResourceBundle bundle = getBndl(resource);
                String msg = bundle.getString(key);
                if (msg != null) {
                    return msg;
                }
            } catch (Exception e) {
                //log.log(MessageLogLevel.ERROR, "EXCEPTION_001", e.getMessage(), e);
            }
        }
        return key;
    }

    public Enumeration<String> getKeys() {
        return new Enumeration<String>() {
            private Iterator<String> itr = resources.iterator();
            private Enumeration<String> currentEnum;

            public boolean hasMoreElements() {
                if (currentEnum == null || !currentEnum.hasMoreElements()) {
                    if (itr.hasNext()) {
                        currentEnum = getBndl(itr.next()).getKeys();
                    }
                }
                if (currentEnum != null) {
                    return currentEnum.hasMoreElements();
                }
                return false;
            }


            public String nextElement() {
                return currentEnum.nextElement();
            }
        };
    }

    private ResourceBundle getBndl(String bundle) {
        return ResourceBundle.getBundle(bundle, locale);
    }
}
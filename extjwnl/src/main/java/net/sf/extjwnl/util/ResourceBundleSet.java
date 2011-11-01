package net.sf.extjwnl.util;

import java.util.*;

/**
 * A ResourceBundle that is a proxy to multiple ResourceBundles.
 *
 * @author John Didion <jdidion@didion.net>
 */
public class ResourceBundleSet extends ResourceBundle {

    private Locale locale = Locale.getDefault();
    private final Set<String> resources = new HashSet<String>();

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
            ResourceBundle bundle = getBndl(resource);
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        return key;
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
                //noinspection SimplifiableIfStatement
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
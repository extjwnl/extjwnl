package net.didion.jwnl.util;

import java.util.*;

/**
 * A ResourceBundle that is a proxy to multiple ResourceBundles.
 */
public class ResourceBundleSet extends ResourceBundle {
    private Locale _locale = Locale.getDefault();
    private List<String> _resources = new ArrayList<String>();

    public ResourceBundleSet(String resource) {
        addResource(resource);
    }

    public ResourceBundleSet(String[] resources) {
        for (String resource : resources) {
            addResource(resource);
        }
    }

    public void addResource(String resource) {
        _resources.add(resource);
    }

    public String[] getResources() {
        return _resources.toArray(new String[_resources.size()]);
    }

    public void setLocale(Locale locale) {
        _locale = locale;
    }

    protected Object handleGetObject(String key) {
        for (Object _resource : _resources) {
            try {
                ResourceBundle bundle = getBndl((String) _resource);
                String msg = bundle.getString(key);
                if (msg != null) {
                    return msg;
                }
            } catch (Exception ex) {
            }
        }
        return key;
    }

    public Enumeration getKeys() {
        return new Enumeration() {
            private Iterator _itr = _resources.iterator();
            private Enumeration _currentEnum;

            public boolean hasMoreElements() {
                if (_currentEnum == null || !_currentEnum.hasMoreElements()) {
                    if (_itr.hasNext()) {
                        _currentEnum = getBndl((String) _itr.next()).getKeys();
                    }
                }
                if (_currentEnum != null) {
                    return _currentEnum.hasMoreElements();
                }
                return false;
            }


            public Object nextElement() {
                return _currentEnum.nextElement();
            }
        };
    }

    private ResourceBundle getBndl(String bundle) {
        return ResourceBundle.getBundle(bundle, _locale);
    }
}
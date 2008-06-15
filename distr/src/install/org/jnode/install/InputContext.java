/*
 * $Id$
 */
package org.jnode.install;

import java.util.HashMap;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class InputContext {
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public abstract String getStringInput(String message);

    public void setStringValue(String key, String value) {
        values.put(key, value);
    }

    public String getStringValue(String key) {
        return (String) values.get(key);
    }
}

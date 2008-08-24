/*
 * $
 */
package org.jnode.vm;

import java.util.Properties;

/**
 * @author Levente S\u00e1ntha
 */
public class VmSystemSettings {
    private static final boolean ENABLED = false;

    public static void insertSystemProperties(Properties props) {
        if (ENABLED) {
            props.put("java.home", "@java.home@");
            props.put("java.io.tmpdir", "@java.io.tmpdir@");
            props.put("user.home", "@user.home@");
        }
    }
}

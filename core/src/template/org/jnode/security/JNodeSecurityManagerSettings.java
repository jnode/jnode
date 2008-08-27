package org.jnode.security;

import java.security.Permission;

/**
 * @author Levente S\u00e1ntha
 */
public class JNodeSecurityManagerSettings {
    private static final boolean ENABLED = false;
    static void checkPermission(Permission perm, JNodeSecurityManager sm) {
        boolean enabled = true;
        if (ENABLED) {
            enabled = Boolean.valueOf("@jnode.security.enabled@");
        }
        if (enabled){
            sm.defaultCheckPermission(perm);
        }
    }
}

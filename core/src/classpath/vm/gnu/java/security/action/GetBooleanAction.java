/*
 * $Id$
 */
package gnu.java.security.action;

import java.security.PrivilegedAction;

/**
 * Utility class for getting Boolean properties in a privileged action.
 * 
 * @see Boolean#getBoolean(String)
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GetBooleanAction implements PrivilegedAction {

    private final String key;

    public GetBooleanAction(String key) {
        this.key = key;
    }

    /**
     * @see java.security.PrivilegedAction#run()
     */
    public Object run() {
        return Boolean.valueOf(Boolean.getBoolean(key));
    }
}

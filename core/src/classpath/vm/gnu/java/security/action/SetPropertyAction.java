/*
 * $Id$
 */
package gnu.java.security.action;

import java.security.PrivilegedAction;


/**
 * Utility class for setting properties in a privileged action.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SetPropertyAction implements PrivilegedAction {

    private final String key;
    private final String value;
    
    /**
     * Initialize this instance.
     * @param key
     */
    public SetPropertyAction(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    /**
     * Set the property
     * @see java.security.PrivilegedAction#run()
     */
    public Object run() {
        System.setProperty(key, value);
        return null;
    }
}

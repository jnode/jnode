/*
 * $Id$
 */
package gnu.java.security.action;

import java.security.PrivilegedAction;


/**
 * Utility class for getting all system properties in a privileged action.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GetPropertiesAction implements PrivilegedAction {

    /**
     * @see java.security.PrivilegedAction#run()
     */
    public Object run() {
        return System.getProperties();
    }
}

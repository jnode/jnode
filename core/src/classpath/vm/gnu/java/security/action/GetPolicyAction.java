/*
 * $Id$
 */
package gnu.java.security.action;

import java.security.Policy;
import java.security.PrivilegedAction;

/**
 * Utility class for getting the current Policy in a privileged action.
 * 
 * @see java.security.Policy
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GetPolicyAction implements PrivilegedAction {

    private static final GetPolicyAction instance = new GetPolicyAction();
    
    /**
     * Gets the single instance of this class.
     * @return
     */
    public static GetPolicyAction getInstance() {
        return instance;
    }
    
    /**
     * @see java.security.PrivilegedAction#run()
     */
    public Object run() {
        return Policy.getPolicy();
    }
    
    /**
     * Singleton constructor.
     */
    private GetPolicyAction() {
        // Do nothing
    }
}

/*
 * $Id$
 */
package gnu.java.security.action;

import java.security.PrivilegedAction;


/**
 * Utility class for getting Integer properties in a privileged action.
 * 
 * @see Integer#getInteger(String, Integer)
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GetIntegerAction implements PrivilegedAction {

    private final String key;
    private final Integer defaultValue;
    
    public GetIntegerAction(String key) {
        this(key, null);
    }
    
    public GetIntegerAction(String key, int defaultValue) {
        this(key, new Integer(defaultValue));
    }

    public GetIntegerAction(String key, Integer defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
    
    /**
     * @see java.security.PrivilegedAction#run()
     */
    public Object run() {
        return Integer.getInteger(key, defaultValue);
    }
}

/*
 * $Id$
 */
package gnu.java.security.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;


/**
 * Utility class for invoking a method in a privileged action.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeAction implements PrivilegedExceptionAction {

    private final Method method;
    private final Object object;
    private final Object[] args;
    
    /**
     * Initialize this instance.
     * @param method
     * @param object
     * @param args
     */
    public InvokeAction(Method method, Object object, Object[] args) {
        this.method = method;
        this.object = object;
        this.args = args;
    }
    
    /**
     * @see java.security.PrivilegedExceptionAction#run()
     */
    public Object run() throws IllegalAccessException, InvocationTargetException {
        return method.invoke(object, args);
    }
}

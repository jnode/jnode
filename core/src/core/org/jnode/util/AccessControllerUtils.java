/*
 * $Id$
 */
package org.jnode.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AccessControllerUtils {

    /**
     * Calls AccessController.doPrivileged and unwraps any exception wrapped 
     * in the PrivilegedActionException.
     * 
     * @param action
     * @return
     * @throws Exception
     */
    public static Object doPrivileged(PrivilegedExceptionAction action)
    throws Exception {
        try {
            return AccessController.doPrivileged(action);
        } catch (PrivilegedActionException ex) {
            throw ex.getException();
        }
    }

}

/*
 * $Id$
 */
package org.jnode.security;

import java.security.BasicPermission;

/**
 * JNode specific permission.
 * 
 * Known permission names:
 * <ul>
 *   <li>getVmClass
 *   <li>getVmThread
 * </ul>
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodePermission extends BasicPermission {

    /**
     * @param name
     */
    public JNodePermission(String name) {
        super(name);
    }
    
    /**
     * @param name
     * @param actions
     */
    public JNodePermission(String name, String actions) {
        super(name, actions);
    }
}

/*
 * $Id$
 */
package org.jnode.driver;

import java.security.BasicPermission;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DriverPermission extends BasicPermission {

    /**
     * @param name
     */
    public DriverPermission(String name) {
        super(name);
    }
    
    /**
     * @param name
     * @param actions
     */
    public DriverPermission(String name, String actions) {
        super(name, actions);
    }
}

/*
 * $Id$
 */
package org.jnode.net;

import java.security.BasicPermission;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetPermission extends BasicPermission {

    /**
     * @param name
     */
    public NetPermission(String name) {
        super(name);
    }
    
    /**
     * @param name
     * @param actions
     */
    public NetPermission(String name, String actions) {
        super(name, actions);
    }
}

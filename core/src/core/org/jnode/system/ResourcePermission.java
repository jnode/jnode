/*
 * $Id$
 */
package org.jnode.system;

import gnu.java.security.util.SimplePermissionCollection;

import java.security.Permission;
import java.security.PermissionCollection;

/**
 * Permission required for allocating system resources.
 * 
 * The following permissions names are supported.
 * 
 * <ul>
 * <li>"ioports"
 * <li>"memory:" ( "dma" | "normal" )
 * <li>"irq"
 * </ul>
 * 
 * There is no actions attribute.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ResourcePermission extends Permission {

    /**
     * @param name
     */
    public ResourcePermission(String name) {
        super(name);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof ResourcePermission) {
            return ((ResourcePermission) obj).getName().equals(getName());
        } else {
            return false;
        }
    }

    /**
     * @see java.security.Permission#getActions()
     */
    public String getActions() {
        return "";
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * @see java.security.Permission#implies(java.security.Permission)
     */
    public boolean implies(Permission perm) {
        if (!(perm instanceof ResourcePermission)) {
            return false;
        }
        
        return getName().equals(perm.getName());
    }    
    
    /**
     * @see java.security.Permission#newPermissionCollection()
     */
    public PermissionCollection newPermissionCollection() {
        return new SimplePermissionCollection();
    }
}

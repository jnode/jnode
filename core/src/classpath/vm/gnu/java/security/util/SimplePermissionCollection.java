/*
 * $Id$
 */
package gnu.java.security.util;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/**
 * A PermissionCollection that stores all added permissions in a list and implies
 * a certain permission "A" if there is at least one added permission that implies the
 * permission "A".
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SimplePermissionCollection extends PermissionCollection {

    private final Vector list = new Vector();
    
    /**
     * @see java.security.PermissionCollection#add(java.security.Permission)
     */
    public void add(Permission perm) {
        if (isReadOnly()) {
            throw new SecurityException("Cannot add to readonly collection");
        }
        list.add(perm);
    }
    
    /**
     * @see java.security.PermissionCollection#elements()
     */
    public Enumeration elements() {
        return list.elements();
    }
    
    /**
     * @see java.security.PermissionCollection#implies(java.security.Permission)
     */
    public boolean implies(Permission perm) {
        for (Iterator i = list.iterator(); i.hasNext(); ) {
            final Permission p = (Permission)i.next();
            if (p.implies(perm)) {
                return true;
            }
        }
        return false;
    }
}

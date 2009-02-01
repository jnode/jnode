/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package gnu.java.security.util;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A PermissionCollection that stores all added permissions in a list and implies
 * a certain permission "A" if there is at least one added permission that implies the
 * permission "A".
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SimplePermissionCollection extends PermissionCollection {

    private final Vector<Permission> list = new Vector<Permission>();
    
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
        for (Permission p : list) {
            if (p.implies(perm)) {
                return true;
            }
        }
        return false;
    }
}

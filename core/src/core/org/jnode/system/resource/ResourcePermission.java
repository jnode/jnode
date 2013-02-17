/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.system.resource;

import gnu.java.security.util.SimplePermissionCollection;

import java.security.Permission;
import java.security.PermissionCollection;

/**
 * Permission required for allocating system resources.
 * <p/>
 * The following permissions names are supported.
 * <p/>
 * <ul>
 * <li>"ioports"
 * <li>"memory:" ( "dma" | "normal" )
 * <li>"irq"
 * </ul>
 * <p/>
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

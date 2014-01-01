/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package java.security;

import org.jnode.vm.VmAccessControlContext;

/**
 * @see java.security.AccessControlContext
 */
public class NativeAccessControlContext {

    /**
     * @see java.security.AccessControlContext#createContext(AccessControlContext)
     */
    private static Object createContext(AccessControlContext instance) {
        return new VmAccessControlContext(null, (VmAccessControlContext) instance.vmContext);
    }

    /**
     * @see java.security.AccessControlContext#createContext2(ProtectionDomain[])
     */
    private static Object createContext2(ProtectionDomain[] context) {
        return new VmAccessControlContext(context, null);
    }

    /**
     * @see java.security.AccessControlContext#checkPermission(Permission)
     */
    private static void checkPermission(AccessControlContext instance, Permission perm) throws AccessControlException {
        ((VmAccessControlContext)instance.vmContext).checkPermission(perm);
    }
}

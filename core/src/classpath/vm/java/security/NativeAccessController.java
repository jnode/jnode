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

import org.jnode.vm.VmAccessController;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmAccessControlContext;

/**
 * @see java.security.AccessController
 */
public class NativeAccessController {
    /**
     * @see java.security.AccessController#checkPermission(Permission)
     */
    private static void checkPermission(Permission perm) throws AccessControlException {
        VmAccessController.checkPermission(perm);
    }

    /**
     * @see java.security.AccessController#doPrivileged(PrivilegedAction)
     */
    private static <T> T doPrivileged(PrivilegedAction<T> action) {
        if (action == null) {
            Unsafe.debug("action == null!! ");
        }
        return (T) VmAccessController.doPrivileged(action, null);
    }

    /**
     * @see java.security.AccessController#doPrivileged(PrivilegedAction, AccessControlContext)
     */
    private static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context) {
        return (T) VmAccessController.doPrivileged(action, (VmAccessControlContext) context.getVmContext());
    }

    /**
     * @see java.security.AccessController#doPrivileged(PrivilegedExceptionAction)
     */
    private static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        return (T) VmAccessController.doPrivileged(action, null);
    }

    /**
     * @see java.security.AccessController#doPrivileged(PrivilegedExceptionAction, AccessControlContext)
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action, AccessControlContext context)
        throws PrivilegedActionException {
        return (T)VmAccessController.doPrivileged(action, (VmAccessControlContext) context.getVmContext());
    }

    /**
     * @see java.security.AccessController#getContext()
     */
    public static AccessControlContext getContext() {
        return new AccessControlContext(VmAccessController.getContext());
    }
}

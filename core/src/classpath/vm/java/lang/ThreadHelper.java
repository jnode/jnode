/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package java.lang;

import org.jnode.permission.JNodePermission;
import org.jnode.vm.scheduler.VmThread;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.Internal;

/**
 *
 */
public class ThreadHelper {
    private static final JNodePermission GETVMTHREAD_PERM = new JNodePermission("getVmThread");

    /**
     * Gets the internal thread representation
     *
     * @param thread
     * @return
     */
    public static VmThread getVmThread(Thread thread) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GETVMTHREAD_PERM);
        }
        return (VmThread) thread.vmThread;
    }

    /**
     * Gets the internal thread representation.
     * Used for kernel space access.
     *
     * @param thread
     * @return
     */
    @KernelSpace
    @Internal
    public static VmThread getVmThreadKS(Thread thread) {
        return (VmThread) thread.vmThread;
    }
}

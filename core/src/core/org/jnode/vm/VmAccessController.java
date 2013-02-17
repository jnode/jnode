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
 
package org.jnode.vm;

import java.security.AccessControlException;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import org.jnode.annotation.CheckPermission;
import org.jnode.annotation.DoPrivileged;
import org.jnode.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmThread;

/**
 * JNode VM implementation of the java AccessControl system.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmAccessController {

    /**
     * Checks whether the access control context of the current thread allows the
     * given Permission. Throws an <code>AccessControlException</code> when
     * the permission is not allowed in the current context. Otherwise returns
     * silently without throwing an exception.
     *
     * @param perm the permission to be checked.
     * @throws AccessControlException thrown if the current context does not allow the given
     *                                permission.
     */

    @CheckPermission
    public static void checkPermission(Permission perm)
        throws AccessControlException {
        if (!VmProcessor.current().isThreadSwitchActive()) {
            // getContext().checkPermission(perm);

            // This is an optimized version of
            // getContext().checkPermission()
            // that does not require any memory allocations.
            final VmStackReader reader = VmProcessor.current()
                .getArchitecture().getStackReader();
            final VmStackFrameEnumerator sfEnum = new VmStackFrameEnumerator(reader);
            int recursionCount = 0;
            while (sfEnum.isValid()) {
                final VmMethod method = sfEnum.getMethod();
                if (method.hasDoPrivilegedPragma()) {
                    // Stop here with the current thread's stacktrace.
                    break;
                } else if (method.hasCheckPermissionPragma()) {
                    // Be paranoia for now, let's check for recursive
                    // checkPermission calls.
                    recursionCount++;
                    if (recursionCount > 2) {
                        reader.debugStackTrace();
                        Unsafe.die("Recursive checkPermission");
                    }
                } else {
                    final VmType<?> declClass = method.getDeclaringClass();
                    final ProtectionDomain pd = declClass.getProtectionDomain();
                    if (pd != null) {
                        // Unsafe.debug(":pd");
                        if (!pd.implies(perm)) {
                            // Unsafe.debug("Permission denied");
                            throw new AccessControlException("Permission \""
                                + perm + "\" not granted due to "
                                + declClass.getName());
                        }
                    }
                }
                if (method.hasPrivilegedActionPragma()) {
                    // Break here, do not include inherited thread context
                    return;
                }
                sfEnum.next();
            }

            final VmThread thread = VmThread.currentThread();
            final VmAccessControlContext inheritedCtx = thread.getContext();
            if (inheritedCtx != null) {
                inheritedCtx.checkPermission(perm);
            }
        }
    }

    /**
     * This method takes a "snapshot" of the current calling context, which
     * includes the current Thread's inherited AccessControlContext, and places
     * it in an AccessControlContext object. This context may then be checked at
     * a later point, possibly in another thread.
     *
     * @return the AccessControlContext based on the current context.
     */
    public static VmAccessControlContext getContext() {
        final VmStackReader reader = VmProcessor.current()
            .getArchitecture().getStackReader();
        final VmStackFrame[] stack = reader.getVmStackTrace(VmMagic
            .getCurrentFrame(), null, Integer.MAX_VALUE);
        final int count = stack.length;
        final ProtectionDomain domains[] = new ProtectionDomain[count];

        for (int i = 0; i < count; i++) {
            final VmMethod method = stack[i].getMethod();
            if (method.hasDoPrivilegedPragma()) {
                // Stop here
                break;
            } else if (method.hasPrivilegedActionPragma()) {
                // Break here, do not include inherited thread context
                return new VmAccessControlContext(domains, null);
            } else {
                domains[i] = method.getDeclaringClass().getProtectionDomain();
            }
        }
        final VmThread thread = VmThread.currentThread();
        return new VmAccessControlContext(domains, thread.getContext());
    }

    /**
     * Calls the <code>run()</code> method of the given action with as
     * (initial) access control context the given context combined with the
     * protection domain of the calling class. Calls to
     * <code>checkPermission()</code> in the <code>run()</code> method
     * ignore all earlier protection domains of classes in the call chain, but
     * add checks for the protection domains given in the supplied context.
     *
     * @param action  the <code>PrivilegedAction</code> whose <code>run()</code>
     *                should be be called.
     * @param context the <code>AccessControlContext</code> whose protection
     *                domains should be added to the protection domain of the
     *                calling class.
     * @return the result of the <code>action.run()</code> method.
     */
    @DoPrivileged
    public static Object doPrivileged(PrivilegedAction action,
                                      VmAccessControlContext context) {
        final VmThread thread = VmThread.currentThread();
        final VmAccessControlContext prevContext = thread.getContext();
        thread.setContext(context);
        try {
            return action.run();
        } finally {
            thread.setContext(prevContext);
        }
    }

    /**
     * Calls the <code>run()</code> method of the given action with as
     * (initial) access control context the given context combined with the
     * protection domain of the calling class. Calls to
     * <code>checkPermission()</code> in the <code>run()</code> method
     * ignore all earlier protection domains of classes in the call chain, but
     * add checks for the protection domains given in the supplied context. If
     * the <code>run()</code> method throws an exception then this method will
     * wrap that exception in an <code>PrivilegedActionException</code>.
     *
     * @param action  the <code>PrivilegedExceptionAction</code> whose
     *                <code>run()</code> should be be called.
     * @param context the <code>AccessControlContext</code> whose protection
     *                domains should be added to the protection domain of the
     *                calling class.
     * @throws PrivilegedActionException wrapped around any exception that is thrown in the
     *                                   <code>run()</code> method.
     * @return the result of the <code>action.run()</code> method.
     */
    @DoPrivileged
    public static Object doPrivileged(PrivilegedExceptionAction action,
                                      VmAccessControlContext context) throws PrivilegedActionException {
        final VmThread thread = VmThread.currentThread();
        final VmAccessControlContext prevContext = thread.getContext();
        thread.setContext(context);
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
        } finally {
            thread.setContext(prevContext);
        }
    }
}

/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.jnode.vm;

import java.security.AccessControlException;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.vmmagic.pragma.PrivilegedActionPragma;

/**
 * JNode VM implementation of the java AccessControl system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmAccessController {

    /**
     * Checks wether the access control context of the current thread allows
     * the given Permission. Throws an <code>AccessControlException</code>
     * when the permission is not allowed in the current context. Otherwise
     * returns silently without throwing an exception.
     * 
     * @param perm
     *            the permission to be checked.
     * @exception AccessControlException
     *                thrown if the current context does not allow the given
     *                permission.
     */

    public static void checkPermission(Permission perm)
            throws AccessControlException, PragmaCheckPermission {
        if (!Unsafe.getCurrentProcessor().isThreadSwitchActive()) {
            //getContext().checkPermission(perm);

            // This is an optimized version of
            // getContext().checkPermission()
            // that does not require any memory allocations.
            final VmStackReader reader = Unsafe.getCurrentProcessor()
                    .getArchitecture().getStackReader();
            VmAddress sf = Unsafe.getCurrentFrame();
            int recursionCount = 0;
            while (reader.isValid(sf)) {
                final VmMethod method = reader.getMethod(sf);
                if (method.canThrow(PragmaDoPrivileged.class)) {
                    // Stop here with the current thread's stacktrace.
                    break;
                } else if (method.canThrow(PragmaCheckPermission.class)) {
                    // Be paranoia for now, let's check for recursive
                    // checkPermission calls.
                    recursionCount++;
                    if (recursionCount > 2) {
                        reader.debugStackTrace();
                        Unsafe.die("Recursive checkPermission");
                    }
                } else {
                    final VmType declClass = method.getDeclaringClass();
                    final ProtectionDomain pd = declClass.getProtectionDomain();
                    if (pd != null) {
                        //Unsafe.debug(":pd");
                        if (!pd.implies(perm)) { 
                        //Unsafe.debug("Permission denied");
                        throw new AccessControlException("Permission \"" + perm
                                + "\" not granted due to " + declClass.getName()); }
                    }
                }
                if (method.canThrow(PrivilegedActionPragma.class)) { 
                    // Break here, do not include inherited thread context
                    return; }
                sf = reader.getPrevious(sf);
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
     * it in an AccessControlContext object. This context may then be checked
     * at a later point, possibly in another thread.
     * 
     * @return the AccessControlContext based on the current context.
     */
    public static VmAccessControlContext getContext() {
        final VmStackReader reader = Unsafe.getCurrentProcessor()
                .getArchitecture().getStackReader();
        final VmStackFrame[] stack = reader.getVmStackTrace(Unsafe
                .getCurrentFrame(), null, Integer.MAX_VALUE);
        final int count = stack.length;
        final ProtectionDomain domains[] = new ProtectionDomain[ count];

        for (int i = 0; i < count; i++) {
            final VmMethod method = stack[ i].getMethod();
            if (method.canThrow(PragmaDoPrivileged.class)) {
                // Stop here
                break;
            } else if (method.canThrow(PrivilegedActionPragma.class)) {
                // Break here, do not include inherited thread context
                return new VmAccessControlContext(domains, null);
            } else {
                domains[ i] = method.getDeclaringClass().getProtectionDomain();
            }
        }
        final VmThread thread = VmThread.currentThread();
        return new VmAccessControlContext(domains, thread.getContext());
    }

    /**
     * Calls the <code>run()</code> method of the given action with as
     * (initial) access control context the given context combined with the
     * protection domain of the calling class. Calls to <code>checkPermission()</code>
     * in the <code>run()</code> method ignore all earlier protection domains
     * of classes in the call chain, but add checks for the protection domains
     * given in the supplied context.
     * 
     * @param action
     *            the <code>PrivilegedAction</code> whose <code>run()</code>
     *            should be be called.
     * @param context
     *            the <code>AccessControlContext</code> whose protection
     *            domains should be added to the protection domain of the
     *            calling class. @returns the result of the <code>action.run()</code>
     *            method.
     */
    public static Object doPrivileged(PrivilegedAction action,
            VmAccessControlContext context) throws PragmaDoPrivileged {
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
     * protection domain of the calling class. Calls to <code>checkPermission()</code>
     * in the <code>run()</code> method ignore all earlier protection domains
     * of classes in the call chain, but add checks for the protection domains
     * given in the supplied context. If the <code>run()</code> method throws
     * an exception then this method will wrap that exception in an <code>PrivilegedActionException</code>.
     * 
     * @param action
     *            the <code>PrivilegedExceptionAction</code> whose <code>run()</code>
     *            should be be called.
     * @param context
     *            the <code>AccessControlContext</code> whose protection
     *            domains should be added to the protection domain of the
     *            calling class. @returns the result of the <code>action.run()</code>
     *            method.
     * @exception PrivilegedActionException
     *                wrapped around any exception that is thrown in the <code>run()</code>
     *                method.
     */
    public static Object doPrivileged(PrivilegedExceptionAction action,
            VmAccessControlContext context) throws PrivilegedActionException,
            PragmaDoPrivileged {
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

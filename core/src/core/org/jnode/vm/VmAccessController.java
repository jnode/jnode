/*
 * $Id$
 */
package org.jnode.vm;

import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import org.jnode.vm.classmgr.VmMethod;

/**
 * JNode VM implementation of the java AccessControl system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmAccessController {

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
            } else {
                domains[ i] = method.getDeclaringClass().getProtectionDomain();
            }
        }
        final VmThread thread = VmThread.currentThread();
        return new VmAccessControlContext(domains, thread.getContext());
    }

    /**
     * Calls the <code>run()</code> method of the given action with as
     * (initial) access control context only the protection domain of the
     * calling class. Calls to <code>checkPermission()</code> in the <code>run()</code>
     * method ignore all earlier protection domains of classes in the call
     * chain. Note that the protection domains of classes called by the code in
     * the <code>run()</code> method are not ignored.
     * 
     * @param action
     *            the <code>PrivilegedAction</code> whose <code>run()</code>
     *            should be be called. @returns the result of the <code>action.run()</code>
     *            method.
     */
    public static Object doPrivileged(PrivilegedAction action)
            throws PragmaDoPrivileged {
        return action.run();
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
     * (initial) access control context only the protection domain of the
     * calling class. Calls to <code>checkPermission()</code> in the <code>run()</code>
     * method ignore all earlier protection domains of classes in the call
     * chain. Note that the protection domains of classes called by the code in
     * the <code>run()</code> method are not ignored. If the <code>run()</code>
     * method throws an exception then this method will wrap that exception in
     * an <code>PrivilegedActionException</code>.
     * 
     * @param action
     *            the <code>PrivilegedExceptionAction</code> whose <code>run()</code>
     *            should be be called. @returns the result of the <code>action.run()</code>
     *            method.
     * @exception PrivilegedActionException
     *                wrapped around any exception that is thrown in the <code>run()</code>
     *                method.
     */
    public static Object doPrivileged(PrivilegedExceptionAction action)
            throws PrivilegedActionException, PragmaDoPrivileged {
        try {
            return action.run();
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
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
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
        } finally {
            thread.setContext(prevContext);
        }
    }
}

/*
 * $Id$
 */
package org.jnode.vm;

import java.security.AccessControlException;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.ArrayList;

/**
 * Representation of the current access control context.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmAccessControlContext {

    private final ProtectionDomain domains[];

    /**
     * Initialize this instance.
     * 
     * @param context
     * @param inheritedContext
     */
    public VmAccessControlContext(ProtectionDomain[] context,
            VmAccessControlContext inheritedContext) {
        final ArrayList ctxList = new ArrayList();
        if (context != null) {
            addUniqueToList(ctxList, context);
        }
        if ((inheritedContext != null) && (inheritedContext.domains != null)) {
            addUniqueToList(ctxList, inheritedContext.domains);
        }
        if (ctxList.isEmpty()) {
            this.domains = null;
        } else {
            this.domains = (ProtectionDomain[]) ctxList
                    .toArray(new ProtectionDomain[ ctxList.size()]);
        }
    }

    /**
     * Determines whether or not the specific permission is granted depending
     * on the context it is within.
     * 
     * @param perm
     *            a permission to check
     * 
     * @throws AccessControlException
     *             if the permssion is not permitted
     */
    public final void checkPermission(Permission perm)
            throws AccessControlException {
        if (domains != null) {
            final int count = domains.length;
            for (int i = 0; i < count; i++) {
                if (!domains[ i].implies(perm)) { throw new AccessControlException(
                        "Permission not granted"); }
            }
        }
    }

    /**
     * Checks if two AccessControlContexts are equal.
     * 
     * It first checks if obj is an AccessControlContext class, and then checks
     * if each ProtectionDomain matches.
     * 
     * @param obj
     *            The object to compare this class to
     * 
     * @return true if equal, false otherwise
     */
    public final boolean equals(Object obj) {
        if (obj instanceof VmAccessControlContext) {
            final VmAccessControlContext acc = (VmAccessControlContext) obj;
            final int count = (domains == null) ? 0 : domains.length;

            if (acc.domains.length != count) { return false; }

            for (int i = 0; i < count; i++) {
                if (acc.domains[ i] != domains[ i]) { return false; }
            }
            return true;
        }
        return false;
    }

    /**
     * Computes a hash code of this class
     * 
     * @return a hash code representing this class
     */
    public final int hashCode() {
        int h = 0;
        final int count = (domains == null) ? 0 : domains.length;
        for (int i = 0; i < count; i++) {
            h ^= domains[ i].hashCode();
        }
        return h;
    }

    private final void addUniqueToList(ArrayList ctxList,
            ProtectionDomain[] context) {
        final int count = context.length;
        for (int i = 0; i < count; i++) {
            final ProtectionDomain pd = context[ i];
            if (pd != null) {
                if (!ctxList.contains(pd)) {
                    ctxList.add(pd);
                }
            }
        }
    }
}

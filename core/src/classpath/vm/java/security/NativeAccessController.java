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

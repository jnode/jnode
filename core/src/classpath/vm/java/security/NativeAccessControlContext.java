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

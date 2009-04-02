package java.lang;

import org.jnode.vm.scheduler.VmThread;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Internal;
import org.jnode.security.JNodePermission;

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

/*
 * $Id$
 */
package java.lang;

import org.jnode.vm.VmSystem;
import org.jnode.vm.scheduler.MonitorManager;

/**
 * @author Levente S\u00e1ntha
 * @see Object
 */
class NativeObject {
    private static void registerNatives() {
    }

    /**
     * @see Object#getClass()
     */
    private static Class<?> getClass(Object instance) {
        return VmSystem.getClass(instance);
    }

    /**
     * @see Object#hashCode()
     */
    private static int hashCode(Object instance) {
        return VmSystem.getHashCode(instance);
    }

    /**
     * @see Object#clone()
     */
    private static Object clone(Object instance) throws CloneNotSupportedException {
        if (instance instanceof Cloneable)
            return VmSystem.clone((Cloneable) instance);
        throw new CloneNotSupportedException("Object not cloneable");
    }

    /**
     * @see java.lang.Object#notify()
     */
    private static void notify(Object instance) {
        MonitorManager.notify(instance);
    }

    /**
     * @see Object#notifyAll() ()
     */
    private static void notifyAll(Object instance) {
        MonitorManager.notifyAll(instance);
    }

    /**
     * @see Object#wait(long)
     */
    private static void wait(Object instance, long timeout) throws InterruptedException {
        MonitorManager.wait(instance, timeout);
    }
}

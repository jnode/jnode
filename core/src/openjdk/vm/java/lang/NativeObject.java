/*
 * $Id$
 */
package java.lang;

/**
 * @author Levente Sántha
 * @see Object
 */
public class NativeObject {
    private static void registerNatives() {
    }

    /**
     * @see Object#getClass()
     */
    public static Class<?> getClass(Object instance) {
        return VMObject.getClass(instance);
    }

    /**
     * @see Object#hashCode()
     */
    public static int hashCode(Object instance) {
        return System.identityHashCode(instance);
    }

    /**
     * @see Object#clone()
     */
    protected static Object clone(Object instance) throws CloneNotSupportedException {
        if (instance instanceof Cloneable)
            return VMObject.clone((Cloneable) instance);
        throw new CloneNotSupportedException("Object not cloneable");
    }

    /**
     * @see java.lang.Object#notify()
     */
    public static void notify(Object instance) {
        VMObject.notify(instance);
    }

    /**
     * @see Object#notifyAll() ()
     */
    public static void notifyAll(Object instance) {
        VMObject.notifyAll(instance);
    }

    /**
     * @see java.lang.Object#wait(long)
     */
    public static void wait(Object instance, long timeout) throws InterruptedException {
        VMObject.wait(instance, timeout, 0);

    }
}

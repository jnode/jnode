/*
 * $Id$
 */
package java.io;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VMObjectInputStream {

    /**
     * This native method is used to get access to the protected method of the
     * same name in SecurityManger.
     * 
     * @param sm
     *            SecurityManager instance which should be called.
     * @return The current class loader in the calling stack.
     */
    static ClassLoader currentClassLoader (SecurityManager sm) {
        // TODO implement me
        return null;
    }
    
    static Object allocateObject (Class clazz)
    throws InstantiationException {
        // TODO implement me
        return null;
    }
    
    static void callConstructor (Class clazz, Object obj) {
        // TODO implement me
    }
    
}

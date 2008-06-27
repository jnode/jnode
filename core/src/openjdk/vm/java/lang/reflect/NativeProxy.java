/*
 * $Id$
 */
package java.lang.reflect;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeProxy {
    /**
     * @see java.lang.reflect.Proxy#defineClass0(ClassLoader, String, byte[], int, int)
     */
    private static Class defineClass0(ClassLoader loader, String name,
					     byte[] b, int off, int len){
        if(loader == null)
            loader = Thread.currentThread().getContextClassLoader();
        return loader.getVmClassLoader().defineClass(name, b, off, len, Object.class.getProtectionDomain()).asClass();
    }
}

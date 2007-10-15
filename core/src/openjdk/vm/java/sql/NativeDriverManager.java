/*
 * $Id$
 */
package java.sql;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeDriverManager {
    /**
     * @see DriverManager#getCallerClassLoader()
     */
    private static ClassLoader getCallerClassLoader(){
        return sun.reflect.Reflection.getCallerClass(3).getClassLoader();
    }
}

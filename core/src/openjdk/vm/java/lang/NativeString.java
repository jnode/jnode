/*
 * $Id$
 */
package java.lang;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeString {
    public static String intern(String instance){
        return VMString.intern(instance);
    }
}

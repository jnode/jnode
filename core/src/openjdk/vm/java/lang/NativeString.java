/*
 * $Id$
 */
package java.lang;

/**
 * @author Levente S�ntha
 */
public class NativeString {
    public static String intern(String instance){
        return VMString.intern(instance);
    }
}

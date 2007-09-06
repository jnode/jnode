/*
 * $Id$
 */
package java.lang;

/**
 * @author Levente Sántha
 */
public class NativeString {
    public static String intern(String instance){
        return VMString.intern(instance);
    }
}

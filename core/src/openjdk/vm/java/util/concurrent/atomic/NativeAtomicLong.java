/*
 * $
 */
package java.util.concurrent.atomic;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeAtomicLong {
    private static boolean VMSupportsCS8() {
        //todo improve it
        return false;
    }
}

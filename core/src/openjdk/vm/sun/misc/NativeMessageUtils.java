/*
 * $Id$
 */
package sun.misc;

/**
 * @author Levente Sántha
 */
public class NativeMessageUtils {
    /**
     *  Print a message directly to stderr, bypassing all the
     *  character conversion methods.
     *  @param msg   message to print
     * @see sun.misc.MessageUtils#toStderr(String)
     */
    public static void toStderr(String msg){
        //todo improve it
        System.err.print(msg);
    }

    /**
     *  Print a message directly to stdout, bypassing all the
     *  character conversion methods.
     *  @param msg   message to print
     * @see sun.misc.MessageUtils#toStdout(String)
     */
    public static void toStdout(String msg){
        //todo improve it
        System.out.print(msg);
    }
}

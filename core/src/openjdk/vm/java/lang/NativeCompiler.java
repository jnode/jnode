/*
 * $Id$
 */
package java.lang;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeCompiler {
    /**
     * @see Compiler#initialize()
     */
    private static void initialize(){}

    /**
     * @see Compiler#registerNatives()
     */
    private static void registerNatives(){}

    /**
     * @see Compiler@compileClass
     */
    public static boolean compileClass(Class<?> clazz){
        //todo implement it
        return false;
    }

    /**
     * @see Compiler#compileClasses(String)
     */
    public static boolean compileClasses(String string){
        //todo implement it
        return false;
    }

    /**
     * @see Compiler#command(Object)
     */
    public static Object command(Object any) {
        return any;
    }

    /**
     * @see Compiler#enable()
     */
    public static void enable(){}

    /**
     * @see Compiler#disable()
     */
    public static void disable(){}
}

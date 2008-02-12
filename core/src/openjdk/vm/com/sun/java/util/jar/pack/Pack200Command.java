/*
 * $
 */
package com.sun.java.util.jar.pack;

/**
 * @author Levente S\u00e1ntha
 */
public class Pack200Command {
    public static void main(String[] argv) throws Exception {
        System.setProperty(Utils.DEBUG_DISABLE_NATIVE, "true");
        String[] args = new String[argv.length + 1];
        args[0] = "--pack";
        for(int i = 0; i < argv.length; i++){
            args[i + 1] = argv[i];
        }
        Driver.main(args);
    }
}

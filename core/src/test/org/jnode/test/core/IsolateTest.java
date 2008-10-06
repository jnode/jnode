/*
 * $Id$
 */
package org.jnode.test.core;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;

public class IsolateTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String mainClass;
        String[] isolateArgs;
        if (args.length > 0) {
            mainClass = args[0];
            isolateArgs = new String[args.length - 1];
            System.arraycopy(args, 1, isolateArgs, 0, args.length - 1);
        } else {
            mainClass = "org.jnode.test.core.IsolatedHelloWorld";
            isolateArgs = new String[0];
        }

        Isolate newIsolate = new Isolate(mainClass, isolateArgs);
        try {
            newIsolate.start();
        } catch (IsolateStartupException e) {
            e.printStackTrace();
        }
    }

}

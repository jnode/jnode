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
        Isolate newIsolate = new Isolate("org.jnode.test.core.IsolatedHelloWorld", new String[0]);
        try {
            newIsolate.start();
        } catch (IsolateStartupException e) {
            e.printStackTrace();
        }
    }

}

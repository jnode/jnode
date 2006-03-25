/*
 * $Id$
 */
package org.jnode.test.core;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.isolate.LinkMessage;

public class IsolateTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Isolate newIsolate = new Isolate("org.jnode.test.core.IsolatedHelloWorld", new String[0]);
        try {
            newIsolate.start(new LinkMessage[0]);
        } catch (IsolateStartupException e) {
            e.printStackTrace();
        }
    }

}

/*
 * $Id$
 */
package org.jnode.test.core;

import java.io.IOException;
import java.io.InterruptedIOException;
import javax.isolate.ClosedLinkException;
import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.isolate.Link;
import javax.isolate.LinkMessage;

public class LinkTest {

    /**
     * @param args
     * @throws IsolateStartupException
     * @throws IOException
     * @throws InterruptedIOException
     */
    public static void main(String[] args) throws IsolateStartupException, InterruptedIOException, IOException {
        String clsName = ChildClass.class.getName();
        Isolate child = new Isolate(clsName, new String[0]);

        Link link = Link.newLink(Isolate.currentIsolate(), child);

        child.start(link);

        link.send(LinkMessage.newStringMessage("Hello world"));
    }

    public static class ChildClass {

        public static void main(String[] args)
            throws ClosedLinkException, IllegalStateException, InterruptedIOException, IOException {
            Link link = Isolate.getLinks()[0];
            LinkMessage msg = link.receive();
            System.out.println("Got message: " + msg.extractString());
        }
    }
}

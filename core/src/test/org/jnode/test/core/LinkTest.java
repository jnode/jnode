/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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

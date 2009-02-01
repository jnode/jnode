/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.test.threads;

import static org.jnode.test.threads.ThreadingUtils.print;
import static org.jnode.test.threads.ThreadingUtils.sleep;
import static org.jnode.test.threads.ThreadingUtils.trackEnter;
import static org.jnode.test.threads.ThreadingUtils.trackExecute;
import static org.jnode.test.threads.ThreadingUtils.trackExit;

/**
 * @author Levente S\u00e1ntha
 */
public class BasicTest {
    public static void main(String[] argv) throws Exception {
        print("Testing thread creation and starting...");
        ThreadingUtils.Forkable f = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                aMethod();
            }
        };
        print("thread starting");
        f.fork();
        print("thread started");
        print("Testing join...");
        print("join in progress");
        f.join();
        print("join completed");

    }

    private static void aMethod() {
        trackEnter();
        try {
            trackExecute();
            sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        trackExit();
    }
}

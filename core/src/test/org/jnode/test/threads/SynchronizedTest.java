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
 
package org.jnode.test.threads;

import static org.jnode.test.threads.ThreadingUtils.EXEC_TIME;
import static org.jnode.test.threads.ThreadingUtils.fork;
import static org.jnode.test.threads.ThreadingUtils.print;
import static org.jnode.test.threads.ThreadingUtils.sleep;
import static org.jnode.test.threads.ThreadingUtils.trackEnter;
import static org.jnode.test.threads.ThreadingUtils.trackExecute;
import static org.jnode.test.threads.ThreadingUtils.trackExit;

/**
 * @author Levente S\u00e1ntha
 */
public class SynchronizedTest {
    private static SynchronizedTest st = new SynchronizedTest();

    public static void main(String[] argv) throws Exception {
        print("Testing basic thread synchronization...");
        ThreadingUtils.Forkable staticSynchronizedMethodLong = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                staticSynchronizedMethodLong();
            }
        };
        ThreadingUtils.Forkable staticSynchronizedMethodShort = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                staticSynchronizedMethodShort();
            }
        };
        ThreadingUtils.Forkable staticSychronizedBlockShort = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                staticSynchronizedBlockShort();
            }
        };
        ThreadingUtils.Forkable synchronizedMethodLong = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                st.synchronizedMethodLong();
            }
        };
        ThreadingUtils.Forkable synchronizedMthodShort = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                st.synchronizedMethodShort();
            }
        };
        ThreadingUtils.Forkable synchronizedBlockShort = new ThreadingUtils.Forkable() {
            public void execute() throws Exception {
                st.synchronizedBlockShort();
            }
        };
        print("synchronized static method with synchronized static method");
        fork(staticSynchronizedMethodLong, staticSynchronizedMethodShort);

        print("synchronized static method with synchronized block on class");
        fork(staticSynchronizedMethodLong, staticSychronizedBlockShort);

        print("synchronized method with synchronized method");
        fork(synchronizedMethodLong, synchronizedMthodShort);

        print("synchronized method with synchronized block on this");
        fork(synchronizedMethodLong, synchronizedBlockShort);

        print("synchronized static method with synchronized method");
        fork(staticSynchronizedMethodLong, synchronizedMthodShort);

        print("synchronized static method with synchronized block on this");
        fork(staticSynchronizedMethodLong, synchronizedBlockShort);

        print("synchronized method with synchronized static method");
        fork(synchronizedMethodLong, staticSynchronizedMethodShort);

        print("synchronized method with synchronized block on class");
        fork(synchronizedMethodLong, synchronizedBlockShort);
    }

    private static synchronized void staticSynchronizedMethodLong() throws Exception {
        trackEnter();
        trackExecute();
        sleep(EXEC_TIME);
        trackExit();
    }

    private static synchronized void staticSynchronizedMethodShort() {
        trackEnter();
        trackExecute();
        trackExit();
    }

    private static void staticSynchronizedBlockShort() {
        trackEnter();
        synchronized (SynchronizedTest.class) {
            trackExecute();
        }
        trackExit();
    }

    synchronized void synchronizedMethodLong() throws Exception {
        trackEnter();
        trackExecute();
        sleep(EXEC_TIME);
        trackExit();
    }

    synchronized void synchronizedMethodShort() {
        trackEnter();
        trackExecute();
        trackExit();
    }

    void synchronizedBlockShort() {
        trackEnter();
        synchronized (this) {
            trackExecute();
        }
        trackExit();
    }
}

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
 
package org.jnode.test.core;

import javax.isolate.Isolate;
import javax.isolate.IsolateStatus;
import javax.isolate.Link;
import javax.isolate.LinkMessage;
import javax.isolate.ClosedLinkException;
import javax.isolate.IsolateStartupException;

public class StatusLinkTest {

    public static void main(String[] args) throws Exception {

        runChild(ChildClass1.class);

        runChild(ChildClass2.class);

        runChild(ChildClass3.class);

        runChild(ChildClass4.class);

        runChild(ChildClass5.class);

        runChild(ChildClass6.class);

        Isolate child = new Isolate(ChildClass7.class.getName());
        new Thread(new StatusMonitor(child.newStatusLink()), "status-monitor").start();
        child.start();

        try {
            Thread.sleep(100);
        } finally {
            child.exit(0);
        }

        child = new Isolate(ChildClass7.class.getName());
        new Thread(new StatusMonitor(child.newStatusLink()), "status-monitor").start();
        child.start();

        try {
            Thread.sleep(100);
        } finally {
            child.halt(0);
        }

    }

    private static Isolate runChild(Class<?> clazz)
        throws ClosedLinkException, IsolateStartupException, InterruptedException {
        Isolate child = new Isolate(clazz.getName());
        Thread moni = new Thread(new StatusMonitor(child.newStatusLink()), "status-monitor");
        moni.start();
        child.start();
        moni.join();
        return child;
    }

    public static class StatusMonitor implements Runnable {
        private final Link link;

        public StatusMonitor(Link link) {
            this.link = link;
        }

        public void run() {
            try {
                while (true) {
                    LinkMessage msg = link.receive();
                    if (msg.containsStatus()) {
                        IsolateStatus is = msg.extractStatus();
                        System.out.println("Got status message: " + is);
                        //org.jnode.vm.Unsafe.debug("Got status message: " + is + "\n");
                        if (is.getState().equals(IsolateStatus.State.EXITED)) {
                            break;
                        }
                    } else {
                        System.out.println("Unknown message: " + msg);
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    public static class ChildClass1 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            System.out.println("Child: sleeping 2 seconds");
            Thread.sleep(2000);
            System.out.println("Child: exiting");
        }
    }

    public static class ChildClass2 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Child thread: started");
                        System.out.println("Child thread: sleeping 2 seconds");
                        Thread.sleep(2000);
                        System.out.println("Child thread: exiting");
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }, "child-thread2").start();
            System.out.println("Child: exiting");
        }
    }

    public static class ChildClass3 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Child thread: started");
                        System.out.println("Child thread: sleeping 2 seconds");
                        Thread.sleep(2000);
                        System.out.println("Child thread: throwing an exception");
                        throw new RuntimeException();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }, "child-thread3").start();

            System.out.println("Child: exiting");
        }
    }

    public static class ChildClass4 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Child thread: started");
                        System.out.println("Child thread: sleeping 2 seconds");
                        Thread.sleep(2000);
                        System.out.println("Child thread: calling System.exit()");
                        System.exit(0);
                        System.out.println("Child thread: exiting");
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }, "child-thread4").start();

            System.out.println("Child: exiting");
        }
    }

    public static class ChildClass5 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Child thread: started");
                        System.out.println("Child thread: sleeping 2 seconds");
                        Thread.sleep(2000);
                        System.out.println("Child thread: callng Runtime.halt()");
                        Runtime.getRuntime().halt(0);
                        System.out.println("Child thread: exiting");
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }, "child-thread5").start();

            System.out.println("Child: exiting");
        }
    }

    public static class ChildClass6 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Child thread: started");
                        System.out.println("Child thread: sleeping 2 seconds");
                        Thread.sleep(2000);
                        System.out.println("Child thread: calling Isolate.exit()");
                        Isolate.currentIsolate().exit(0);
                        System.out.println("Child thread: exiting");
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }, "child-thread6").start();

            System.out.println("Child: exiting");
        }
    }

    public static class ChildClass7 {
        public static void main(String[] args) throws InterruptedException {
            System.out.println("Child: started");
            new Thread(new Runnable() {
                public void run() {
                    System.out.println("Child thread: started");
                    System.out.println("Child thread: working ...");
                    for (int i = 0; i < 100000; i++) {
                       // System.out.println("Child thread: " + i);
                        Math.sin(i);
                        if (i % 100 == 0) {
                            org.jnode.vm.Unsafe.debug("i=" + i + "\n");
                            System.out.println("i=" + i);
                        }
                    }
                    System.out.println("Child thread: exiting");
                }
            }, "child-thread7").start();
            System.out.println("Child: exiting");
        }
    }
}

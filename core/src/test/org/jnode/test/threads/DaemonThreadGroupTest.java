/*
 * $Id: BasicTest.java 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2008 JNode.org
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

/**
 * Test the behaviour of daemon ThreadGroups.  Literally, the javadoc says that
 * a daemon ThreadGroup is destroyed when the last active thread is "stopped".
 * But it really means when it "dies".  There is also the issue of unstarted
 * Threads; do they prevent a daemon ThreadGroup from being destroyed?
 *
 * @author crawley@jnode.org
 */
public class DaemonThreadGroupTest {

    public static class BarbieThread extends Thread {

        public BarbieThread(ThreadGroup tg) {
            super(tg, "barbie");
        }

        @Override
        public void run() {
            // (Don't ask ...)
            try {
                sleep(3000);
            } catch (InterruptedException ex) {
                System.err.println("Barbie: \"what?\"");
            }
            System.err.println("Barbie: \"I'm a Barbie girl, in a Barbie world\"");
            System.err.println("Barbie: \"zzzzzzz ....\"");
            try {
                sleep(3000);
            } catch (InterruptedException ex) {
                System.err.println("Barbie: \"what?\"");
            }
            System.err.println("Barbie: \"Made of plastic, life is so fantastic\"");
        }
    }

    private static void threadGroupTest(ThreadGroup tg1, ThreadGroup tg2, boolean expectDeath)
        throws Exception {
        if (tg1.isDestroyed() || tg2.isDestroyed()) {
            System.err.println("ERROR: already destroyed");
            return;
        }
        BarbieThread bt = new BarbieThread(tg2);
        bt.start();
        if (tg1.isDestroyed()) {
            System.err.println("ERROR: destroyed too soon");
            return;
        }

        bt.join();

        System.err.println("Bye bye Barbie");
        if (tg1.isDestroyed() != expectDeath) {
            System.err.println("ERROR: tg1.isDestroyed() (" + tg1.isDestroyed() +
                ") != expectDeath (" + expectDeath + ")");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.err.println("First test: non-daemon group");
        ThreadGroup tg = new ThreadGroup("test1");
        tg.setDaemon(false);
        threadGroupTest(tg, tg, false);

        System.err.println("Second test: daemon group");
        tg = new ThreadGroup("test2");
        tg.setDaemon(true);
        threadGroupTest(tg, tg, true);

        System.err.println("Third test: daemon group with child group");
        tg = new ThreadGroup("test3");
        ThreadGroup tg2 = new ThreadGroup(tg, "child");
        tg.setDaemon(true);
        tg2.setDaemon(true);
        threadGroupTest(tg, tg2, true);

        System.err.println("Done");
    }
}

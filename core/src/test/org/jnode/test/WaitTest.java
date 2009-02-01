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
 
package org.jnode.test;

/**
 * @author epr
 */
public class WaitTest {

    public static void main(String[] args)
        throws Exception {

        for (int j = 0; j < 20; j++) {
            final WaitTest wt = new WaitTest();

            for (int i = 0; i < 10; i++) {
                final int k = i;
                new Thread(new Runnable() {
                    public void run() {
                        wt.test(k);
                    }
                }).start();
            }

            Thread.sleep(2000);

            wt.trigger();

            // Now test the wait with timeout

            wt.testTimeout();
        }
    }

    private boolean trigger = false;

    public synchronized void test(int i) {
        if (trigger) {
            //System.out.println("Skipping " + i);
        } else {
            try {
                //System.out.println("Before wait " + i);
                wait();
                //System.out.println("After wait " + i);
            } catch (InterruptedException ex) {
                System.out.println("Interrupted " + i);
            }
            //System.out.println("Ready " + i);
        }
    }

    public synchronized void testTimeout() {
        try {
            //System.out.println("Before waitTimeout");
            final long start = System.currentTimeMillis();
            wait(500);
            final long end = System.currentTimeMillis();
            System.out.println("After waitTimeout: it took " + (end - start) + "ms");
        } catch (InterruptedException ex) {
            System.out.println("Interrupted in waitTimeout");
        }
        //System.out.println("Ready waitTimeout");
    }

    public synchronized void trigger() {
        //System.out.println("Before notifyAll");
        trigger = true;
        notifyAll();
        //System.out.println("After notifyAll");
    }

}

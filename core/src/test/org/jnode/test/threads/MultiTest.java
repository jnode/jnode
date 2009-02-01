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

/**
 * @author Levente S\u00e1ntha
 */
public class MultiTest {
    private static int counter;
    private static int threadCounter;

    public static void main(String[] argv) throws InterruptedException {
        print("Testing multiple threads...");
        int n = 10;
        try {
            n = Integer.parseInt(argv[0]);
        } catch (Exception e) {
            //ignore
        }
        Incrementer[] incr = new Incrementer[n];
        Thread[] thr = new Thread[n];
        for (int i = 0; i < n; i++) {
            incr[i] = new Incrementer();
        }

        for (int i = 0; i < n; i++) {
            thr[i] = new Thread(incr[i]);
        }

        for (int i = 0; i < n; i++) {
            thr[i].start();
        }

        for (int i = 0; i < n; i++) {
            thr[i].join();
        }

        for (int i = 0; i < n; i++) {
            thr[i].join();
            ThreadingUtils.print(incr[i].number + "   " + incr[i].i);
        }
        threadCounter = 0;
        counter = 0;
    }

    private static class Incrementer implements Runnable {
        private int i = 0;
        private int number;

        public void run() {
            synchronized (MultiTest.class) {
                number = threadCounter++;
            }
            while (true) {
                synchronized (MultiTest.class) {
                    if (counter >= 1000) break;
                    print(number + " " + counter + " " + ++counter);
                }
                Thread.yield();
                i++;
            }
        }
    }
}

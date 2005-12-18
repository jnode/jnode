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
                number = threadCounter++ ;
            }
            while (true) {
                synchronized (MultiTest.class) {
                    if (counter >= 1000) break;
                    print(number + " " + counter + " " + ++ counter);
                }
                Thread.yield();
                i++;
            }
        }
    }
}

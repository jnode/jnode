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

/**
 * @author Levente S\u00e1ntha
 */
class ThreadingUtils {
    static final int EXEC_TIME = 3;
    static final int PAUSE_TIME = 1;
    static final int UNIT_TIME = 1000;

    static void fork(Forkable... forkable) throws Exception {
        for (Forkable f : forkable) f.fork();
        for (Forkable f : forkable) f.join();
        sleep(PAUSE_TIME);
    }

    static void sleep(int sec) throws Exception {
        Thread.sleep(sec * UNIT_TIME);
    }

    static void print(String str) {
        System.out.println(str);
    }

    static void trackEnter() {
        print(caller(2) + " enter");
    }

    static void trackExecute() {
        print(caller(2) + " execute");
    }

    static void trackExit() {
        print(caller(2) + " exit");
    }

    private static String caller(int i) {
        StackTraceElement elem = new Throwable().getStackTrace()[i];
        return elem.getClassName() + "." + elem.getMethodName();
    }

    abstract static class Forkable implements Runnable {
        private Thread thread;


        public void run() {
            try {
                execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Thread fork() {
            thread = new Thread(this);
            thread.start();
            return thread;
        }

        public void join() throws Exception {
            thread.join();
        }

        public Thread thread() {
            return thread;
        }

        public abstract void execute() throws Exception;
    }
}

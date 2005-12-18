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
    
    static abstract class Forkable implements Runnable {
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

        public void join() throws Exception{
            thread.join();
        }

        public Thread thread() {
            return thread;
        }

        public abstract void execute() throws Exception;
    }
}

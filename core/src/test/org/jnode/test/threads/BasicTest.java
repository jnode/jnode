package org.jnode.test.threads;

import static org.jnode.test.threads.ThreadingUtils.print;
import static org.jnode.test.threads.ThreadingUtils.trackEnter;
import static org.jnode.test.threads.ThreadingUtils.trackExecute;
import static org.jnode.test.threads.ThreadingUtils.sleep;
import static org.jnode.test.threads.ThreadingUtils.trackExit;

/**
 * @author Levente S\u00e1ntha
 */
public class BasicTest {
    public static void main(String[] argv) throws Exception{
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
        try{
            trackExecute();
            sleep(3);
        } catch(Exception e){
            e.printStackTrace();
        }
        trackExit();
    }
}

package gnu.classpath.jdwp;

import gnu.classpath.jdwp.transport.JNodeSocketTransport;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * User: lsantha
 * Date: 6/25/11 5:13 PM
 */
public class Main {
    private static final String str_quit = "Type 'q' to exit";

    private static final int DEFAULT_PORT = 6789;

    private boolean up = true;

    public static void main(String[] args) throws Exception {
        new Main().execute();
    }

    public void execute() throws Exception {
        // FIXME - in the even of internal exceptions, JDWP writes to System.out.
        final String ps = "transport=dt_socket,suspend=n,address=" + DEFAULT_PORT + ",server=y";
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (up()) {
                    Jdwp jdwp = new Jdwp();
                    jdwp.configure(ps);
                    jdwp.run();
                    jdwp.waitToFinish();
                    jdwp.shutdown();
                }
                // workaround for the restricted capabilities of JDWP support in GNU Classpath.
                JNodeSocketTransport.ServerSocketHolder.close();
            }
        });
        t.start();

        Reader in = new InputStreamReader(System.in);
        PrintWriter out = new PrintWriter(System.out);
        while (in.read() != 'q') {
            out.println(str_quit);
        }
        // FIXME - this just stops the 'debug' command.  The listener will keep running
        // until the remote debugger disconnects.  We should have a way to disconnect at
        // this end.
        down();
    }

    public synchronized boolean up() {
        return up;
    }

    public synchronized void down() {
        up = false;
    }
}

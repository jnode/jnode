/*
 * $
 */
package org.jnode.shell.command.debug;

import gnu.classpath.jdwp.JNodeSocketTransport;
import gnu.classpath.jdwp.Jdwp;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * Starts up a JDWP remote debugger listener for this JNode instance.
 *
 * @author Levente S\u00e1ntha
 */
public class DebugCommand extends AbstractCommand {
    private static final int DEFAULT_PORT = 6789;
    private boolean up = true;
    private final IntegerArgument ARG_PORT = 
        new IntegerArgument("port", Argument.OPTIONAL, "the port to listen to");

    public DebugCommand() {
        super("Listen for connections from a remote debugger");
        registerArguments(ARG_PORT);
    }

    public static void main(String[] args) throws Exception {
        new DebugCommand().execute(args);
    }

    @Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        int port = ARG_PORT.isSet() ? ARG_PORT.getValue() : DEFAULT_PORT;

        // FIXME - in the even of internal exceptions, JDWP writes to System.out.
        final String ps = "transport=dt_socket,suspend=n,address=" + port + ",server=y";
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

        while (in.read() != 'q') {
            out.println("Type 'q' to exit");
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

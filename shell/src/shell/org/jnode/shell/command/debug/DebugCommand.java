/*
 * $
 */
package org.jnode.shell.command.debug;

import gnu.classpath.jdwp.Jdwp;
import gnu.classpath.jdwp.JNodeSocketTransport;
import org.jnode.shell.help.argument.IntegerArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * Starts up the remote debugger under JNode.
 *
 * @author Levente S\u00e1ntha
 */
public class DebugCommand {
    private static final int DEFAULT_PORT = 6789;
    private static boolean up = true;
    static final IntegerArgument ARG_PORT = new IntegerArgument("port", "the port to listen to");

	public static Help.Info HELP_INFO = new Help.Info("debug", "Start the remote debugger server.", new Parameter(ARG_PORT, Parameter.OPTIONAL));

	public static void main(String[] argv) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(argv);

        int port = DEFAULT_PORT;
        if(argv.length > 0)
            port = ARG_PORT.getInteger(cmdLine);

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
                //workaround for the restricted capabilities of JDWP support in GNU Classpath.
                JNodeSocketTransport.ServerSocketHolder.close();
                up = true;
            }
        });
        t.start();

        while (System.in.read() != 'q') {
            System.out.println("Type 'q' for exitting.");
        }
        down();
    }

    public static synchronized boolean up() {
        return up;
    }

    public static synchronized void down() {
        up = false;
    }
}

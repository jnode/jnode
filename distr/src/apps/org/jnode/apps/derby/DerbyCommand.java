/*
 * $Id$
 */
package org.jnode.apps.derby;


import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.apache.derby.impl.drda.NetworkServerControlImpl;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * Command for handling Derby server.
 *
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class DerbyCommand extends AbstractCommand {
    private final FileArgument ARG_HOME = new FileArgument(
        "home", Argument.OPTIONAL, "home directory for derby");

    private final FlagArgument FLAG_START = new FlagArgument(
        "start", Argument.OPTIONAL, "if set, start the derby server");

    private final FlagArgument FLAG_STOP = new FlagArgument(
        "stop", Argument.OPTIONAL, "if set, stop the derby server");

    static final IntegerArgument ARG_PORT = new IntegerArgument(
        "port", Argument.OPTIONAL, "jdbc port (default 1527)");

    public DerbyCommand() {
        super("start or stop the derby db server");
        registerArguments(ARG_HOME, ARG_PORT, FLAG_START, FLAG_STOP);
    }

    public static void main(String[] args) throws Exception {
        new DerbyCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
        throws Exception {
        File home_dir = ARG_HOME.getValue();
        String command = FLAG_START.isSet() ? "start" : FLAG_STOP.isSet() ? "stop" : "?";

        // FIXME ... this needs to be passed to the server somehow.
        int port = ARG_PORT.isSet() ? ARG_PORT.getValue() : 1527;

        NetworkServerControlImpl server = new NetworkServerControlImpl();

        try {
            int server_command = server.parseArgs(new String[]{command});

            if (server_command == NetworkServerControlImpl.COMMAND_START) {
                PrintWriter printWriter = new PrintWriter(out);
                server.setLogWriter(printWriter);
                server.start(printWriter);
            } else if (server_command == NetworkServerControlImpl.COMMAND_SHUTDOWN) {
                server.shutdown();
            }

        } catch (Exception e) {
            // FIXME ... don't do this!
            e.printStackTrace();
        }

//    server.executeWork(server_command);

//    NetworkServerControl.main(new String[]{command});
    }
}

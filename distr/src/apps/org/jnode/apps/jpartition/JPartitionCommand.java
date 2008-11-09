package org.jnode.apps.jpartition;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.apps.jpartition.consoleview.ConsoleViewFactory;
import org.jnode.apps.jpartition.swingview.SwingViewFactory;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

public class JPartitionCommand extends AbstractCommand {
    private final FlagArgument FLAG_SWING =
            new FlagArgument("swing", Argument.OPTIONAL, "if set, use the Swing (graphic) UI");

    private final FlagArgument FLAG_CONSOLE =
            new FlagArgument("console", Argument.OPTIONAL, "if set, use the console (text) UI");

    private final FlagArgument ARG_INSTALL =
            new FlagArgument("install", Argument.OPTIONAL, "if set, format the partition(s)");

    public JPartitionCommand() {
        super("interactive disk partitioning tool");
        registerArguments(FLAG_CONSOLE, FLAG_SWING, ARG_INSTALL);
    }

    public static void main(String[] args) throws Exception {
        new JPartitionCommand().execute(args);
    }

    public void execute() throws Exception {
        boolean install = ARG_INSTALL.isSet();

        InputStream in = getInput().getInputStream();
        PrintStream out = getOutput().getPrintStream();
        PrintStream err = getError().getPrintStream();
        ViewFactory viewFactory =
                FLAG_CONSOLE.isSet() ? new ConsoleViewFactory(in, out, err)
                        : FLAG_SWING.isSet() ? new SwingViewFactory() : null;
        if (viewFactory == null) {
            err.println("No UI selected");
            exit(1);
        }

        JPartition jpartition = new JPartition(viewFactory, install);
        jpartition.launch();
    }
}

package org.jnode.shell.command;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;
import org.jnode.shell.help.Help;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Levente S\u00e1ntha
 */
public class ExitCommand {

    public static Help.Info HELP_INFO = new Help.Info(
            "exit",
            "Exit the shell"
    );

    public static void main(String[] args)
            throws Exception {
        new ExitCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine cmdLine,
                        InputStream in, PrintStream out, PrintStream err) throws Exception {
        ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        ((CommandShell) sm.getCurrentShell()).exit();
    }
}

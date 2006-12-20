package org.jnode.shell.command;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.Shell;
import org.jnode.shell.CommandShell;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

import javax.naming.NameNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;

/**
 * Load and execute a command file.
 *
 * @author Levente S\u00e1ntha
 */
public class RunCommand implements Command {

    static final FileArgument ARG_FILE = new FileArgument("file",
            "a command file", false);

    public static Help.Info HELP_INFO = new Help.Info("run",
            "execute a command file", new Parameter[] { new Parameter(
                    RunCommand.ARG_FILE, Parameter.MANDATORY) });

    public static void main(String[] args) throws Exception {
        new RunCommand().execute(new CommandLine(args), System.in,
                System.out, System.err);
    }

    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = RunCommand.HELP_INFO.parse(commandLine.toStringArray());
        final File file = RunCommand.ARG_FILE.getFile(cmdLine);

        Shell shell = null;
        try {
            shell = ShellUtils.getShellManager().getCurrentShell();
        } catch( NameNotFoundException e ) {
            e.printStackTrace();
            return;
        }

        if( shell == null ) {
            System.err.println( "Shell is null." );
            return;
        }

        if(!(shell instanceof CommandShell)) {
            System.err.println("Shell wasn't a CommandShell: " + shell.getClass());
            return;
        }

        ((CommandShell)shell).executeFile(file);
    }
}

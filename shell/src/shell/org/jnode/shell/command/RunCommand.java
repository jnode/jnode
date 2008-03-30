package org.jnode.shell.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

/**
 * Load and execute a command file.
 *
 * @author Levente S\u00e1ntha
 */
public class RunCommand extends AbstractCommand {

    static final FileArgument ARG_FILE = new FileArgument("file",
            "a command file", false);

    public static Help.Info HELP_INFO = new Help.Info("run",
            "execute a command file", new Parameter[] { new Parameter(
                    RunCommand.ARG_FILE, Parameter.MANDATORY) });

    public static void main(String[] args) throws Exception {
        new RunCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = RunCommand.HELP_INFO.parse(commandLine);
        final File file = RunCommand.ARG_FILE.getFile(cmdLine);

        Shell shell = null;
        try {
            shell = ShellUtils.getShellManager().getCurrentShell();
        } catch( NameNotFoundException e ) {
            e.printStackTrace();
            exit(2);
        }

        if( shell == null ) {
            System.err.println( "Shell is null." );
            exit(2);
        }

        if(!(shell instanceof CommandShell)) {
            System.err.println("Shell wasn't a CommandShell: " + shell.getClass());
            exit(2);
        }

        ((CommandShell)shell).executeFile(file);
    }
}

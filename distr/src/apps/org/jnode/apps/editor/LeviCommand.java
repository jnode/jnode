/*
 * $Id$
 */
package org.jnode.apps.editor;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author Levente S\u00e1ntha
 */
public class LeviCommand extends AbstractCommand {
    private final FileArgument ARG_EDIT = new FileArgument(
        "file", Argument.MANDATORY, "the file to edit");

    public LeviCommand() {
        super("LEvi's VIewer\n Q - quit");
        registerArguments(ARG_EDIT);
    }

    public static void main(String[] args) throws Exception {
        new LeedCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
        throws Exception {
        final File file = ARG_EDIT.getValue();
        if (file.isDirectory()) {
            System.err.println(file + " is a directory");
        } else {
            TextEditor.main(new String[]{file.toString(), "ro"});
        }
    }
}

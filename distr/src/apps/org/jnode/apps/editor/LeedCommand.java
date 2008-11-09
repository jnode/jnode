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
public class LeedCommand extends AbstractCommand {
    private final FileArgument ARG_EDIT = new FileArgument(
        "file", Argument.MANDATORY, "the file to edit");

    public LeedCommand() {
        super("LEvi's EDitor\n   Ctrl-S - save\n   Ctrl-Q - quit");
        registerArguments(ARG_EDIT);
    }

    public static void main(String[] args) throws Exception {
        new LeedCommand().execute(args);
    }

    public void execute()
        throws Exception {
        final File file = ARG_EDIT.getValue();
        if (file.isDirectory()) {
            getError().getPrintWriter().println(file + " is a directory");
        } else {
            TextEditor.main(new String[]{file.toString()});
        }
    }
}

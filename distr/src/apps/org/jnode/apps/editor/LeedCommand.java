package org.jnode.apps.editor;

import java.io.File;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

/**
 * @author Levente S\u00e1ntha
 */
public class LeedCommand {
    static final FileArgument ARG_EDIT = new FileArgument("file", "the file to edit");
    public static Help.Info HELP_INFO = new Help.Info(
            "leed", "LEvi's EDitor\n   Ctrl-S - save\n   Ctrl-Q - quit",
            new Parameter[]{new Parameter(ARG_EDIT, Parameter.MANDATORY)}
    );

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final File file = ARG_EDIT.getFile(cmdLine);
        if (file.isDirectory()) {
            System.err.println(file + " is a directory");
        } else {
            TextEditor.main(args);
        }
    }
}

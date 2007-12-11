/*
 * $Id$
 */
package org.jnode.apps.editor;

import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import java.io.File;

/**
 * @author Levente S\u00e1ntha
 */
public class LeviCommand {
    static final FileArgument ARG = new FileArgument("file", "the file to view");
    public static Help.Info HELP_INFO = new Help.Info(
            "levi", "LEvi's VIewer\n Q - quit",
            new Parameter[]{new Parameter(ARG, Parameter.MANDATORY)}
    );

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final File file = ARG.getFile(cmdLine);
        if (file.isDirectory()) {
            System.err.println(file + " is a directory");
        } else {
            TextEditor.main(new String[]{args[0], "ro"});
        }
    }
}

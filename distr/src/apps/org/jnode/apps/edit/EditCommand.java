/**
 * $Id$  
 */
package org.jnode.apps.edit;

import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

import java.io.File;

/**
 * @author Levente Sántha
 */
public class EditCommand {
    static final FileArgument ARG_EDIT = new FileArgument("file", "the file to edit");
    public static Help.Info HELP_INFO = new Help.Info(
            "edit", "edit a file",
            new Parameter[] { new Parameter(ARG_EDIT, Parameter.OPTIONAL)}
    );

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final File file = ARG_EDIT.getFile(cmdLine);
        if(file == null)
            Editor.editFile(null);
        else if (file.exists()) {
            if (file.isDirectory()) {
                System.err.println(file + " is a directory");
            } else {
                Editor.editFile(file);
            }
        } else {
            System.err.println("File does not exist: " + file);
        }
    }
}

/*
 * $Id$
 */
package org.jnode.fs.command;

import java.io.File;

import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 */
public class MkdirCommand {

    static final FileArgument ARG_DIR = new FileArgument("directory", "the directory to create");
    public static Help.Info HELP_INFO =
        new Help.Info(
                "dir",
                "the directory to create",
                new Parameter[] { new Parameter(ARG_DIR, Parameter.MANDATORY)});

    public static void main(String[] args) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(args);

        File dir = ARG_DIR.getFile(cmdLine);
        dir.mkdir();
    }

}

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
 * Touch a file
 * 
 * TODO if file exist change modified date
 * 
 * @author Yves Galante (yves.galante@jmob.net)
 */
public class TouchCommand {

    static final FileArgument ARG_TOUCH = new FileArgument("file",
            "the file to touch");

    public static Help.Info HELP_INFO = new Help.Info("touch",
            "the file to touch", new Parameter[] { new Parameter(ARG_TOUCH,
                    Parameter.MANDATORY)});

    public static void main(String[] args) throws Exception {

        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final File file = ARG_TOUCH.getFile(cmdLine);
        final File parentFile = file.getParentFile();

        if (!file.exists()) {
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    System.err.println("Parent dirs can't create");
                }
            }
            if (file.createNewFile()) {
                System.out.println("File created");
            } else {
                System.err.println("File can't create");
            }
        } else {
            System.out.println("File already exist");
        }
    }
}


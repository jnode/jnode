/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.command.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jnode.command.util.IOUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * Copy standard input to standard output, making a copy in zero or more files.<br>
 * Implementation based on:
 * http://www.opengroup.org/onlinepubs/9699919799/utilities/tee.html.
 */
public class TeeCommand extends AbstractCommand {

    /** Help text for the parameter. */
    private static final String HELP_FILE = "One or more files that will receive the \"tee-d\" output";

    /** Help text for the append switch. */
    private static final String HELP_APPEND = "Append to the given FILEs, do not overwrite";

    /** Help text for the command. */
    private static final String HELP_SUPER = "Copy standard input to each FILE, and also to standard outputd";

    /** The file argument. */
    private final FileArgument argFile;

    /** The append switch. */
    private final FlagArgument argAppend;

    /** The standard input. */
    private InputStream stdin;

    /** The standard output. */
    private OutputStream stdout;

    /** The files that will receive the output. */
    private File[] files;

    /** The return code of the command. */
    private int returnCode = 0;

    /** Flag to indicate if the files should be appended. */
    private boolean appendFiles;

    /**
     * In the documentation it is stated that a minimum of 13 files must be
     * supported. Limit the command to only accept a maximum of 13 files to
     * limit resource usage.
     */
    private static final int MAX_NUMBER_OF_FILES = 13;

    /** Buffer used in stream copies. */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Default constructor.
     */
    public TeeCommand() {
        super(HELP_SUPER);
        int fileFlags = Argument.MULTIPLE;
        argFile = new FileArgument("files", fileFlags, HELP_FILE);
        argAppend = new FlagArgument("append", 0, HELP_APPEND);
        registerArguments(argFile, argAppend);
    }

    /**
     * Used to run this command independently.
     * 
     * @param args the arguments passed to the command
     * @throws Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        new TeeCommand().execute(args);
    }

    @Override
    public void execute() throws IOException {
        OutputStream out = null;
        byte[] buffer = new byte[BUFFER_SIZE];

        stdin = getInput().getInputStream();
        stdout = getOutput().getOutputStream();

        // Parse the command arguments
        files = argFile.getValues();

        if (files == null || files.length == 0) {
            // The command is simply ignored if there are no files
            // Just copy the input to the output and return
            IOUtils.copyStream(stdin, stdout, buffer);
            exit(returnCode);
        }

        if (argAppend.isSet()) {
            appendFiles = true;
        }

        // A maximum of MAX_NUMBER_OF_FILES will be used
        int numberOfFiles = files.length > MAX_NUMBER_OF_FILES ? MAX_NUMBER_OF_FILES : files.length;

        int successfullOpenFiles = 0;

        // Create an array to hold all output files
        OutputStream[] outFiles = new OutputStream[numberOfFiles];

        try {
            // Open all output files
            for (int i = 0; i < numberOfFiles; i++) {
                if ((out = IOUtils.openOutputstream(files[i], appendFiles)) != null) {
                    outFiles[successfullOpenFiles++] = out;
                }
            }

            int count = 0;
            do {
                count = stdin.read(buffer);
                for (int i = 0; i < successfullOpenFiles; i++) {
                    try {
                        outFiles[i].write(buffer, 0, count);
                    } catch (IOException e) {
                        // Only the return code is updated
                        returnCode++;
                    }

                }
                stdout.write(buffer, 0, count);
            } while (!(count < BUFFER_SIZE));

        } finally {
            // Close all open output files
            for (int i = 0; i < successfullOpenFiles; i++) {
                try {
                    outFiles[i].close();
                } catch (IOException e) {
                    // Nothing to do in this case
                    // Just continue closing the files
                }
            }
        }

        // Returns the code
        exit(returnCode);
    }
}

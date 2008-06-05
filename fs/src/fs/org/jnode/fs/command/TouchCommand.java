/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * Touch a file; i.e. create it if it doesn't exist and change its
 * modification timestamp if it does exist.
 * 
 * @author Yves Galante (yves.galante@jmob.net)
 * @author Andreas H\u00e4nel
 */
public class TouchCommand extends AbstractCommand {

    private final FileArgument ARG_FILE = new FileArgument(
            "file", Argument.MANDATORY, "the file to touch");

    public TouchCommand() {
        super("touch a file");
        registerArguments(ARG_FILE);
    }

    public static void main(String[] args) throws Exception {
        new TouchCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
    throws Exception {
        File file = ARG_FILE.getValue();
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                // FIXME ... this is wrong.  Touch should not do this.
                if (!parentFile.mkdirs()) {
                    err.println("Cannot create parent directories");
                    exit(2);
                }
            }            
            if (file.createNewFile()) {
                out.println("File created");
            } else {
                err.println("Cannot create file");
                exit(1);
            }
        } else {
            file.setLastModified(System.currentTimeMillis());
        }
    }
}

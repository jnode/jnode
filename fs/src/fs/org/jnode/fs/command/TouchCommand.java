/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
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

    private static final String help_file = "the file to touch";
    private static final String help_super = "touch a file";
    private static final String err_parent_dir = "Cannot create parent directories";
    private static final String str_created = "File created";
    private static final String err_file = "Cannot create file";
    
    private final FileArgument argFile = new FileArgument("file", Argument.MANDATORY, help_file);

    public TouchCommand() {
        super(help_super);
        registerArguments(argFile);
    }

    public static void main(String[] args) throws Exception {
        new TouchCommand().execute(args);
    }

    public void execute() throws Exception {
        File file = argFile.getValue();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                // FIXME ... this is wrong.  Touch should not do this.
                if (!parentFile.mkdirs()) {
                    err.println(err_parent_dir);
                    exit(2);
                }
            }            
            if (file.createNewFile()) {
                out.println(str_created);
            } else {
                err.println(err_file);
                exit(1);
            }
        } else {
            file.setLastModified(System.currentTimeMillis());
        }
    }
}

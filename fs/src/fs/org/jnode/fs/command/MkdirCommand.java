/*
 * $Id$
 *
 * JNode.org
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
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 */
public class MkdirCommand extends AbstractCommand {

    private final FileArgument ARG_DIR = new FileArgument(
            "directory", Argument.MANDATORY | Argument.NONEXISTENT, "the directory to create");

    public MkdirCommand() {
        super("Create a new directory");
        registerArguments(ARG_DIR);
    }

    public static void main(String[] args) throws Exception {
        new MkdirCommand().execute(args);
    }

    public void execute() {
        File dir = ARG_DIR.getValue();
        PrintWriter err = getError().getPrintWriter();
        if (dir.exists()) {
            err.println(dir.getPath() + " already exists.");
            exit(1);
        }
        if (!dir.mkdir()) {
            err.println("Can't create directory.");
            exit(1);
        }
    }
}

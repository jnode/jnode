/*
 * $Id$
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
import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author Guillaume BINET (gbin@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 */
public class MkdirCommand extends AbstractCommand {

    private static final String help_dir = "the directory to create";
    private static final String help_super = "Create a new directory";
    private static final String fmt_exists = "%s already exists%n";
    private static final String err_cant_create = "Cannot create directory";
    
    private final FileArgument argDir;

    public MkdirCommand() {
        super(help_super);
        argDir = new FileArgument("directory", Argument.MANDATORY | Argument.NONEXISTENT, help_dir);
        registerArguments(argDir);
    }

    public static void main(String[] args) throws Exception {
        new MkdirCommand().execute(args);
    }

    public void execute() {
        File dir = argDir.getValue();
        PrintWriter err = getError().getPrintWriter();
        if (dir.exists()) {
            err.format(fmt_exists, dir);
            exit(1);
        }
        if (!dir.mkdir()) {
            err.println(err_cant_create);
            exit(1);
        }
    }
}

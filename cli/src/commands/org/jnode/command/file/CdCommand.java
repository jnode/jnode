/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * The CdCommand class changes the current directory as given by the "user.dir" property.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 */
public class CdCommand extends AbstractCommand {

    private static final String HELP_DIR = "the directory to change to";
    private static final String HELP_SUPER = "Change the current directory";
    private static final String ERR_HOME = "user.home is not set";
    private static final String ERR_DIR_INVALID = "%s is not a valid directory%n";
    
    private final FileArgument argDir;

    public CdCommand() {
        super(HELP_SUPER);
        argDir = new FileArgument("directory", Argument.OPTIONAL | Argument.EXISTING, HELP_DIR);
        registerArguments(argDir);
    }

    public static void main(String[] args) throws Exception {
        new CdCommand().execute(args);
    }

    public void execute() 
        throws IOException {
        File dir = argDir.getValue();
        PrintWriter err = getError().getPrintWriter();
        if (dir == null) {
            // If no directory argument was given, change to the "user.home" directory.
            String home = System.getProperty("user.home");
            if (home == null || home.isEmpty()) {
                err.println(ERR_HOME);
                exit(1);
            }
            dir = new File(home);
        }
        if (dir.exists() && dir.isDirectory()) {
            System.setProperty("user.dir", dir.getAbsoluteFile().getCanonicalPath());
        } else {
            err.format(ERR_DIR_INVALID, dir);
            exit(1);
        }
    }

}

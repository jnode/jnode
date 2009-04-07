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

package org.jnode.shell.command;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * @author chris boertien
 */
public class HeadCommand extends AbstractCommand {

    private static final String help_files = "Print the first 10 lines of each file to stdout, with multiple files a " +
                                             "header giving the file name is printed. With no file, or if file is - " +
                                             "read standard in.";
    private static final String help_bytes = "output the first <int> bytes, or output all but the last -<int> bytes.";
    private static final String help_lines = "output the first <int> lines, or output all but the last -<int> lines.";
    private static final String help_quiet = "never output headers giving file names";
    private static final String help_verbose = "always output headers giving file names";
    
    private final FileArgument Files = new FileArgument("files", Argument.MULTIPLE | Argument.OPTIONAL, help_files);
    private final IntegerArgument Lines = 
        new IntegerArgument("lines", Argument.OPTIONAL | Argument.EXISTING, help_lines);
    private final IntegerArgument Bytes =
        new IntegerArgument("bytes", Argument.OPTIONAL | Argument.EXISTING, help_bytes);
    private final FlagArgument Quiet = new FlagArgument("quiet", Argument.OPTIONAL, help_quiet);
    private final FlagArgument Verbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
    
    public HeadCommand() {
        super("Print the head of a list of files, or stdin");
        registerArguments(Files, Lines, Bytes, Quiet, Verbose);
    }
    
    public void execute() {
        exit(1);
    }
}

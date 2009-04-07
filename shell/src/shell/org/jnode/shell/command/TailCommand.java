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
public class TailCommand extends AbstractCommand {
    
    private static final String help_files = "Print the last 10 lines of each file to stdout, with multiple files a " +
                                             "header giving the file name is printed. With no file, or if file is - " +
                                             "read standard in.";
    private static final String help_bytes = "output the last <int> bytes, or output from +<int> bytes into the file " +
                                             "to the end.";
    private static final String help_follow = "keep the file open, outputing data as the file grows.";
    private static final String help_follow_retry = "same as -f --retry";
    private static final String help_lines = "Output the last <int> lines instead of, or +<int> to start printing " +
                                             "from that line";
    private static final String help_unchanged = "with -f, reopen the file when the size has not change for <int> " +
                                                 "iterations to see if it has be unlinked or renamed Default is 5";
    private static final String help_pid = "with -f, terminate after process PID does (how?)";
    private static final String help_sleep = "with -f, sleep for <int> seconds between iterations. Default is 1";
    private static final String help_quiet = "never output headers giving file names";
    private static final String help_verbose = "always output headers giving file names";
    private static final String help_retry = "keep trying to open a file even if it is inaccessible at the start, or " +
                                             "if it becomes inaccessible.";
    
    private final FileArgument Files = new FileArgument("files", Argument.OPTIONAL | Argument.MULTIPLE, help_files);
    private final IntegerArgument Bytes = 
        new IntegerArgument("bytes", Argument.EXISTING | Argument.OPTIONAL, help_bytes);
    private final IntegerArgument Lines = 
        new IntegerArgument("lines", Argument.EXISTING | Argument.OPTIONAL, help_lines);
    // FIXME this has to be able to handle -f --follow and --folow=<file>
    private final FlagArgument Follow = new FlagArgument("follow", Argument.OPTIONAL, help_follow);
    private final FlagArgument FollowR = new FlagArgument("followr", Argument.OPTIONAL, help_follow_retry);
    private final IntegerArgument MaxUnchanged = new IntegerArgument("unchanged", Argument.OPTIONAL, help_unchanged);
    // TODO This might work as thread id, since we dont have process ids
    private final IntegerArgument PID = new IntegerArgument("pid", Argument.OPTIONAL, help_pid);
    private final IntegerArgument Sleep = new IntegerArgument("sleep", Argument.OPTIONAL, help_sleep);
    private final FlagArgument Retry = new FlagArgument("retry", Argument.OPTIONAL, help_retry);
    private final FlagArgument Quiet = new FlagArgument("quiet", Argument.OPTIONAL, help_quiet);
    private final FlagArgument Verbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
    
    public TailCommand() {
        super("Print the tail end of a list of files, or stdin.");
        registerArguments(Files, Bytes, Lines, Follow, Retry, FollowR, MaxUnchanged, PID, Sleep, Quiet, Verbose);
    }
    
    public void execute() {
        exit(1);
    }
}

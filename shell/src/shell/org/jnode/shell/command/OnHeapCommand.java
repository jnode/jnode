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
 
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.IntegerArgument;
import org.jnode.shell.help.argument.LongArgument;
import org.jnode.shell.help.argument.SizeArgument;
import org.jnode.vm.Vm;
import org.jnode.vm.memmgr.HeapStatistics;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class OnHeapCommand implements Command {

    private static final IntegerArgument ARG_MININSTANCECOUNT = new IntegerArgument(
            "mic", "the minimum instance count to show");

    private static final SizeArgument ARG_MINTOTALSIZE = new SizeArgument(
            "mts", "the minimum total size to show");

    private static final Parameter PARAM_MININSTANCECOUNT = new Parameter(
            ARG_MININSTANCECOUNT, Parameter.OPTIONAL);

    private static final Parameter PARAM_MINTOTALSIZE = new Parameter(
            ARG_MINTOTALSIZE, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info("onheap",
            "show the number of instances on the heap with memory usage",
            new Parameter[] { PARAM_MININSTANCECOUNT , PARAM_MINTOTALSIZE });

    public static void main(String[] args) throws Exception {
        new OnHeapCommand().execute(null, System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        final ParsedArguments args = HELP_INFO.parse(commandLine);

        out.println("on heap:");
        final HeapStatistics stats = Vm.getHeapManager().getHeapStatistics();
        
        if (PARAM_MININSTANCECOUNT.isSet(args)) {
            stats.setMinimumInstanceCount(ARG_MININSTANCECOUNT.getInteger(args));
        }
        if (PARAM_MINTOTALSIZE.isSet(args)) {
            stats.setMinimumTotalSize(ARG_MINTOTALSIZE.getLong(args));
        }

        out.println(stats.toString());
    }

}

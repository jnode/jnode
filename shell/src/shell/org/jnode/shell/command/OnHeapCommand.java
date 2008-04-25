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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.LongArgument;
import org.jnode.vm.Vm;
import org.jnode.vm.memmgr.HeapStatistics;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class OnHeapCommand extends AbstractCommand {

    private final IntegerArgument ARG_MIN_INSTANCE_COUNT = new IntegerArgument(
            "minCount", Argument.OPTIONAL, 1, Integer.MAX_VALUE, "the minimum instance count to show");

    private final LongArgument ARG_MIN_TOTAL_SIZE = new LongArgument(
            "minTotalSize", Argument.OPTIONAL, 1L, Long.MAX_VALUE, "the minimum total size to show");

    public OnHeapCommand() {
        super("show the number of instances on the heap with memory usage");
        registerArguments(ARG_MIN_INSTANCE_COUNT, ARG_MIN_TOTAL_SIZE);
    }

    public static void main(String[] args) throws Exception {
        new OnHeapCommand().execute(null, System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        out.println("on heap:");
        final HeapStatistics stats = Vm.getHeapManager().getHeapStatistics();
        
        if (ARG_MIN_INSTANCE_COUNT.isSet()) {
            stats.setMinimumInstanceCount(ARG_MIN_INSTANCE_COUNT.getValue());
        }
        if (ARG_MIN_TOTAL_SIZE.isSet()) {
            stats.setMinimumTotalSize(ARG_MIN_TOTAL_SIZE.getValue());
        }

        out.println(stats.toString());
    }

}

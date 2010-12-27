/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.command.system;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.LongArgument;
import org.jnode.vm.facade.HeapStatistics;
import org.jnode.vm.facade.VmUtils;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class OnHeapCommand extends AbstractCommand {
    
    private static final String help_inst = "the minimum instance count to show";
    private static final String help_size = "the minimum total size to show";
    private static final String help_super = "Show the number of instances on the heap with memory usage";
    private static final String str_on_heap = "On Heap:";
    
    private final IntegerArgument argMinInstanceCount;
    private final LongArgument argMinTotalSize;

    public OnHeapCommand() {
        super(help_super);
        argMinInstanceCount = new IntegerArgument("minCount", Argument.OPTIONAL, 1, Integer.MAX_VALUE, help_inst);
        argMinTotalSize     = new LongArgument("minTotalSize", Argument.OPTIONAL, 1L, Long.MAX_VALUE, help_size);
        registerArguments(argMinInstanceCount, argMinTotalSize);
    }

    public static void main(String[] args) throws Exception {
        new OnHeapCommand().execute(args);
    }

    /**
     * Execute this command
     */
    @Override
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        out.println(str_on_heap);
        final HeapStatistics stats = VmUtils.getVm().getHeapManager().getHeapStatistics();
        
        if (argMinInstanceCount.isSet()) {
            stats.setMinimumInstanceCount(argMinInstanceCount.getValue());
        }
        if (argMinTotalSize.isSet()) {
            stats.setMinimumTotalSize(argMinTotalSize.getValue());
        }

        stats.writeTo(new BufferedWriter(getOutput().getWriter(), 2048));
    }

}

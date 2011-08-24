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
import org.jnode.shell.syntax.StringArgument;
import org.jnode.vm.facade.HeapStatistics;
import org.jnode.vm.facade.ObjectFilter;
import org.jnode.vm.facade.SimpleObjectFilter;
import org.jnode.vm.facade.VmUtils;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class OnHeapCommand extends AbstractCommand {
    
    private static final String HELP_INST = "the minimum instance count to show";
    private static final String HELP_SIZE = "the minimum total size to show";
    private static final String HELP_CLASSNAME = "the classname filter";
    private static final String HELP_SUPER = "Show the number of instances on the heap with memory usage";
    private static final String STR_ON_HEAP = "On Heap:";
    
    private final IntegerArgument argMinInstanceCount;
    private final LongArgument argMinTotalSize;
    private final StringArgument className;

    public OnHeapCommand() {
        super(HELP_SUPER);
        argMinInstanceCount = new IntegerArgument("minCount", Argument.OPTIONAL, 1, Integer.MAX_VALUE, HELP_INST);
        argMinTotalSize     = new LongArgument("minTotalSize", Argument.OPTIONAL, 1L, Long.MAX_VALUE, HELP_SIZE);
        className           = new StringArgument("className", Argument.OPTIONAL | Argument.MULTIPLE, HELP_CLASSNAME);
        registerArguments(argMinInstanceCount, argMinTotalSize, className);
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
        out.println(STR_ON_HEAP);
        
        ObjectFilter filter = null;
        if (className.isSet()) {
            SimpleObjectFilter f = new SimpleObjectFilter();
            f.setClassName(className.getValues());
            filter = f;
        }
        
        final HeapStatistics stats = VmUtils.getVm().getHeapManager().getHeapStatistics(filter);
        
        if (argMinInstanceCount.isSet()) {
            stats.setMinimumInstanceCount(argMinInstanceCount.getValue());
        }
        if (argMinTotalSize.isSet()) {
            stats.setMinimumTotalSize(argMinTotalSize.getValue());
        }

        BufferedWriter writer = new BufferedWriter(getOutput().getWriter(), 2048);
        try {
            stats.writeTo(writer);
        } finally {
            writer.flush();
        }
    }

}

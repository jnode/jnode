/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.EnumArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.util.NumberUtils;
import org.jnode.vm.facade.GCStatistics;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.memmgr.VmHeapManager;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class GcCommand extends AbstractCommand {

    private static final String help_debug_flags = "the heap debug flags";
    private static final String help_set = "set these debug flags";
    private static final String help_clear = "clear these debug flags";
    private static final String help_show = "show the debug flags";
    private static final String help_super = "Run the garbage collector";
    private static final String fmt_out = "%11s: %s%n";
    private static final String str_mem_size = "Memory size";
    private static final String str_mem_free = "Free memory";
    private static final String str_time = "Time taken";
    private static final String str_stats = "Stats";
    private static final String str_start = "Starting gc";
    private static final String str_no_flags = "No heap debug flags set";
    private static final String fmt_flags = "Heap debug flags: %s";

    private enum HeapFlag {
        TRACE_BASIC(VmHeapManager.TRACE_BASIC),
        TRACE_ALLOC(VmHeapManager.TRACE_ALLOC),
        TRACE_TRIGGER(VmHeapManager.TRACE_TRIGGER),
        TRACE_OOM(VmHeapManager.TRACE_OOM),
        TRACE_AD_HOC(VmHeapManager.TRACE_AD_HOC),
        all(-1); // All flags

        public final int flagBit;

        private HeapFlag(int flagBit) {
            this.flagBit = flagBit;
        }
    }

    private class HeapDebugFlagArgument extends EnumArgument<HeapFlag> {
        public HeapDebugFlagArgument() {
            super("debugFlag", Argument.OPTIONAL | Argument.MULTIPLE, HeapFlag.class, help_debug_flags);
        }

        @Override
        protected String argumentKind() {
            return "debug flag";
        }
    }

    private final HeapDebugFlagArgument argDebugFlags;
    private final FlagArgument argSet;
    private final FlagArgument argClear;
    private final FlagArgument argShow;

    public GcCommand() {
        super(help_super);
        argDebugFlags = new HeapDebugFlagArgument();
        argSet = new FlagArgument("set", Argument.OPTIONAL, help_set);
        argClear = new FlagArgument("clear", Argument.OPTIONAL, help_clear);
        argShow = new FlagArgument("show", Argument.OPTIONAL, help_show);
        registerArguments(argDebugFlags, argClear, argSet, argShow);
    }

    public static void main(String[] args) throws Exception {
        new GcCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        final PrintWriter out = getOutput().getPrintWriter();
        if (argSet.isSet()) {
            VmUtils.getVm().getHeapManager().setHeapFlags(getFlags());
        } else if (argClear.isSet()) {
            int flags = VmUtils.getVm().getHeapManager().getHeapFlags() ^ getFlags();
            VmUtils.getVm().getHeapManager().setHeapFlags(flags);
        } else if (argShow.isSet()) {
            showFlags(VmUtils.getVm().getHeapManager().getHeapFlags(), out);
        } else {
            final Runtime rt = Runtime.getRuntime();
            out.format(fmt_out, str_mem_size, NumberUtils.toBinaryByte(rt.totalMemory()));
            out.format(fmt_out, str_mem_free, NumberUtils.toBinaryByte(rt.freeMemory()));

            out.println(str_start);
            long start = System.currentTimeMillis();
            rt.gc();
            GCStatistics stats = VmUtils.getVm().getHeapManager().getStatistics();
            Thread.yield();
            long end = System.currentTimeMillis();

            out.format(fmt_out, str_mem_size, NumberUtils.toBinaryByte(rt.totalMemory()));
            out.format(fmt_out, str_mem_free, NumberUtils.toBinaryByte(rt.freeMemory()));
            out.format(fmt_out, str_time, (end - start) + "ms");
            out.format(fmt_out, str_stats, stats.toString());
        }
    }

    private void showFlags(int flags, PrintWriter out) {
        StringBuilder sb = new StringBuilder();
        for (int flagBitMask = 1; flagBitMask != 0; flagBitMask = flagBitMask << 1) {
            if ((flags & flagBitMask) != 0) {
                for (HeapFlag hf : HeapFlag.values()) {
                    if (hf.flagBit == flagBitMask) {
                        sb.append(' ').append(hf.name());
                        break;
                    }
                }
            }
        }
        if (sb.length() == 0) {
            out.println(str_no_flags);
        } else {
            out.format(fmt_flags, sb.toString());
        }
    }

    private int getFlags() {
        int debugFlags = 0;
        if (argDebugFlags.isSet()) {
            for (HeapFlag flag : argDebugFlags.getValues()) {
                debugFlags |= flag.flagBit;
            }
        }
        return debugFlags;
    }
}

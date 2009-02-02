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

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.EnumArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Vm;
import org.jnode.vm.memmgr.GCStatistics;
import org.jnode.vm.memmgr.VmHeapManager;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class GcCommand extends AbstractCommand {

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
            super("debugFlag", Argument.OPTIONAL | Argument.MULTIPLE,
                    HeapFlag.class, "the heap debug flags");
        }
        @Override
        protected String argumentKind() {
            return "debug flag";
        }
    }

    private final HeapDebugFlagArgument ARG_DEBUG_FLAGS = new HeapDebugFlagArgument();
    private final FlagArgument ARG_SET =
        new FlagArgument("set", Argument.OPTIONAL, "set these debug flags");
    private final FlagArgument ARG_CLEAR =
        new FlagArgument("clear", Argument.OPTIONAL, "clear these debug flags");
    private final FlagArgument ARG_SHOW =
        new FlagArgument("show", Argument.OPTIONAL, "show the debug flags");

    public GcCommand() {
        super("Run the garbage collector");
        registerArguments(ARG_DEBUG_FLAGS, ARG_CLEAR, ARG_SET, ARG_SHOW);
    }

    public static void main(String[] args) throws Exception {
        new GcCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        final PrintWriter out = getOutput().getPrintWriter();
        if (ARG_SET.isSet()) {
            Vm.getHeapManager().setHeapFlags(getFlags());
        } else if (ARG_CLEAR.isSet()) {
            int flags = Vm.getHeapManager().getHeapFlags() ^ getFlags();
            Vm.getHeapManager().setHeapFlags(flags);
        } else if (ARG_SHOW.isSet()) {
            showFlags(Vm.getHeapManager().getHeapFlags(), out);
        } else {
            final Runtime rt = Runtime.getRuntime();
            out.println("Memory size: " + NumberUtils.toBinaryByte(rt.totalMemory()));
            out.println("Free memory: " + NumberUtils.toBinaryByte(rt.freeMemory()));

            out.println("Starting gc...");
            long start = System.currentTimeMillis();
            rt.gc();
            GCStatistics stats = Vm.getHeapManager().getStatistics();
            Thread.yield();
            long end = System.currentTimeMillis();

            out.println("Memory size: " + NumberUtils.toBinaryByte(rt.totalMemory()));
            out.println("Free memory: " + NumberUtils.toBinaryByte(rt.freeMemory()));
            out.println("Time taken : " + (end - start) + "ms");
            out.println("Stats      : " + stats.toString());
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
            out.println("No heap debug flags set");
        } else {
            out.println("Heap debug flags:" + sb.toString());
        }
    }

    private int getFlags() {
        int debugFlags = 0;
        if (ARG_DEBUG_FLAGS.isSet()) {
            for (HeapFlag flag : ARG_DEBUG_FLAGS.getValues()) {
                debugFlags |= flag.flagBit;
            }
        }
        return debugFlags;
    }
}

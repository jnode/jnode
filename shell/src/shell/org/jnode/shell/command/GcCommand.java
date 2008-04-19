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
import org.jnode.util.NumberUtils;
import org.jnode.vm.Vm;
import org.jnode.vm.memmgr.GCStatistics;

/**
 * @author epr
 */
public class GcCommand extends AbstractCommand {

    public GcCommand() {
        super("Run the garbage collector");
    }

	public static void main(String[] args) throws Exception {
		new GcCommand().execute(args);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err)
		throws Exception {
			
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
		out.println("Time taken : " + (end-start) + "ms");
        out.println("Stats      : " + stats.toString());
	}

}

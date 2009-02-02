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
 
package org.jnode.shell.command.system;

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.vm.scheduler.VmProcessor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CpuIDCommand extends AbstractCommand {

    public CpuIDCommand() {
        super("Show the identification of the current CPU");
    }

    public static void main(String[] args) throws Exception {
        new CpuIDCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() {
        final VmProcessor cpu = VmProcessor.current();
        PrintWriter out = getOutput().getPrintWriter();
        out.println(cpu.getCPUID());
        out.println(cpu.getPerformanceCounters());
    }
}

/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
import java.util.List;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.vm.Vm;
import org.jnode.vm.scheduler.VmProcessor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class VmInfoCommand extends AbstractCommand {
    
    private final FlagArgument FLAG_RESET = 
        new FlagArgument("reset", Argument.OPTIONAL, "if set, reset the JNode VM counters");
    
    public VmInfoCommand() {
        super("prints JNode VM info");
        registerArguments(FLAG_RESET);
    }

    public static void main(String[] args) throws Exception {
        new VmInfoCommand().execute(args);
    }
    
    @Override
    public void execute() {
        final Vm vm = Vm.getVm();
        if (vm != null && !vm.isBootstrap()) {
            PrintWriter out = getOutput().getPrintWriter();
            out.println("JNode VM " + vm.getVersion());
            vm.dumpStatistics(out);
            vm.getSharedStatics().dumpStatistics(out);
            Vm.getHeapManager().dumpStatistics(out);
            final SecurityManager sm = System.getSecurityManager();
            out.println("Security manager: " + sm);
            List<VmProcessor> processors = vm.getProcessors();
            for (VmProcessor cpu : processors) {
                out.println("Processor " + processors.indexOf(cpu) + " (" + cpu.getIdString() + ")");
                cpu.dumpStatistics(out);
            }
            if (FLAG_RESET.isSet()) {
                vm.resetCounters();
            }
        }
    }
}

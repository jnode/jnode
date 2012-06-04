/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
import java.util.List;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.vm.facade.VmProcessor;
import org.jnode.vm.facade.VmUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class VmInfoCommand extends AbstractCommand {
    
    private static final String help_reset = "if set, reset the JNode VM counters";
    private static final String help_super = "Prints JNode VM info";
    private static final String fmt_vm = "JNode VM %s%n";
    private static final String fmt_sm = "Security Manager %s%n";
    private static final String fmt_proc = "Processor %s (%s)%n";
    
    private final FlagArgument argReset;
    
    public VmInfoCommand() {
        super(help_super);
        argReset = new FlagArgument("reset", Argument.OPTIONAL, help_reset);
        registerArguments(argReset);
    }

    public static void main(String[] args) throws Exception {
        new VmInfoCommand().execute(args);
    }
    
    @Override
    public void execute() {
        final org.jnode.vm.facade.Vm vm = VmUtils.getVm();
        if (vm != null && !vm.isBootstrap()) {
            PrintWriter out = getOutput().getPrintWriter();
            out.format(fmt_vm, vm.getVersion());
            VmUtils.dumpStatistics(out);
            vm.getSharedStatics().dumpStatistics(out);
            VmUtils.getVm().getHeapManager().dumpStatistics(out);
            final SecurityManager sm = System.getSecurityManager();
            out.format(fmt_sm, sm);
            List<VmProcessor> processors = vm.getProcessors();
            for (VmProcessor cpu : processors) {
                out.format(fmt_proc, processors.indexOf(cpu), cpu.getIdString());
                cpu.dumpStatistics(out);
            }
            if (argReset.isSet()) {
                VmUtils.resetCounters();
            }
        }
    }
}

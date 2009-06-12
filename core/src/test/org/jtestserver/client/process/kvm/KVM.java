/*
JTestServer is a client/server framework for testing any JVM implementation.

 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.client.process.kvm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jtestserver.client.process.VmManager;
import org.jtestserver.client.utils.CommandLineBuilder;
import org.jtestserver.client.utils.ProcessRunner;
import org.jtestserver.client.utils.SystemUtils;
import org.jtestserver.client.utils.SystemUtils.ProcessStatus;

/**
 * Implementation of {@link VmManager} for the 
 * <a href="http://kvm.qumranet.com/kvmwiki/Front_Page">Kernel Virtual Machine</a> (KVM).
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class KVM implements VmManager {
    /**
     * Option used to specify the actual operating system to run in KVM.
     */
    private static final String OPTION_CDROM = "-cdrom";
    
    private static final String KVM_COMMAND = "kvm";
    
    /**
     * The {@link ProcessRunner} used to manage the new KVM process.
     */
    private final ProcessRunner runner = new ProcessRunner();
    
    /**
     * Configuration used to build the command line that will launch KVM.
     */
    private final KVMConfig config;
    
    private final String startCommandLine;
    
    /**
     * 
     * @param config Configuration used to build the command line that will launch KVM.
     */
    public KVM(KVMConfig config) {
        this.config = config;
        this.startCommandLine = createStartCommandLine().toString();
        this.config.setVmName(startCommandLine);
    }
    
    /**
     * {@inheritDoc}
     * The implementation is launching {@code ps} through a command line.
     */
    @Override
    public List<String> getRunningVMs() throws IOException {
        List<ProcessStatus> processes = SystemUtils.getInstance().getProcessStatus(KVM_COMMAND);
        List<String> runningVMs = new ArrayList<String>(processes.size());
        for (ProcessStatus ps : processes) {
            if (ps.getArguments().equals(startCommandLine)) {
                runningVMs.add(ps.getArguments());
            }
        }
        return runningVMs;
    }

    /**
     * {@inheritDoc}
     * The implementation is launching {@code kvm} through a command line.  
     */
    @Override
    public boolean start(String vm) throws IOException {
        return runner.execute(createStartCommandLine());
    }

    /**
     * {@inheritDoc}
     * The implementation is launching {@code kill} through a command line.
     */
    @Override
    public boolean stop(String vm) throws IOException {
        boolean success = true;
        for (ProcessStatus ps : SystemUtils.getInstance().getProcessStatus(KVM_COMMAND)) {
            if (ps.getArguments().equals(vm)) {
                success &= SystemUtils.getInstance().killProcess(ps.getIdentifier());
            }
        }
        
        return success;
    }
    
    private CommandLineBuilder createStartCommandLine() {
        CommandLineBuilder cmdLine = new CommandLineBuilder(KVM_COMMAND); 
        cmdLine.append("-m").append(config.getMemory());
        cmdLine.append(OPTION_CDROM).append(config.getCdrom().getAbsolutePath());
        cmdLine.append(config.getOptions()); 
        cmdLine.append("-serial").append(config.getSerial());
        cmdLine.append("-k").append(config.getKeyboard());
        return cmdLine;
    }
}

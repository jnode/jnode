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
package org.jtestserver.client.process.vmware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jtestserver.client.process.VmManager;
import org.jtestserver.client.utils.PipeInputStream;
import org.jtestserver.client.utils.ProcessRunner;
import org.jtestserver.client.utils.PipeInputStream.Listener;

/**
 * Implementation of {@link VmManager} for <a href="http://www.vmware.com/">VMware</a>.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class VMware implements VmManager {
    /**
     * The {@link ProcessRunner} used to manage the new VMware process.
     */
    private ProcessRunner runner = new ProcessRunner();
    
    /**
     * Base command needed by all commands sent to VMware server.
     */
    private final String[] baseCommand;
    
    /**
     * 
     * @param config
     */
    public VMware(VMwareConfig config) {
        String url = "http://" + config.getHost() + ":" + config.getPort() + "/sdk";
        baseCommand = new String[] {"vmrun", "-T", "server", "-h", url, "-u", 
            config.getUserName(), "-p", config.getPassword()};
    }
    
    /**
     * {@inheritDoc}
     * The implementation is using the command line to communicate with the VMware server.
     */
    @Override
    public boolean start(String vm) throws IOException {
        return executeCommand("start", vm);
    }
    
    /**
     * {@inheritDoc}
     * The implementation is using the command line to communicate with the VMware server.
     */
    @Override
    public boolean stop(String vm) throws IOException {        
        return executeCommand("stop", vm);
    }

    /**
     * {@inheritDoc}
     * The implementation is using the command line to communicate with the VMware server.
     */
    @Override
    public List<String> getRunningVMs() throws IOException {
        final List<String> runningVMs = new ArrayList<String>();
        boolean success  = executeCommand(new PipeInputStream.Listener() {

            @Override
            public void lineReceived(String line) {
                runningVMs.add(line);
            }
            
        }, "list");
        
        if (!success) {
            throw new IOException("failed to get running VMs");
        }
        
        return runningVMs;        
    }

    /**
     * Helper method that execute a command by appending the {@link #baseCommand} 
     * with the given parameters.
     * 
     * @param command parameters to append to the {@link #baseCommand}
     * @return true on success.
     * @throws IOException
     */
    private boolean executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    /**
     * Helper method that execute a command by appending the {@link #baseCommand} 
     * with the given parameters.
     * 
     * @param listener an optional {@link Listener} that will receive standard and error 
     * outputs from the process.
     * 
     * @param command parameters to append to the {@link #baseCommand}
     * @return true on success.
     * @throws IOException
     */
    private boolean executeCommand(Listener listener, String... command) throws IOException {
        String[] fullCommand = new String[baseCommand.length + command.length];
        System.arraycopy(baseCommand, 0, fullCommand, 0, baseCommand.length);
        System.arraycopy(command, 0, fullCommand, baseCommand.length, command.length);
        
        return runner.executeAndWait(listener, fullCommand);
    }
}

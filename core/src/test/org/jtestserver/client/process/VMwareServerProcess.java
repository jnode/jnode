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
package org.jtestserver.client.process;

import java.io.IOException;
import java.util.logging.Logger;

import org.jtestserver.client.Config;

public class VMwareServerProcess implements ServerProcess {
    private static final Logger LOGGER = Logger.getLogger(VMwareServerProcess.class.getName());

    private final String vmName;
    private final VMware vmware; 
    public VMwareServerProcess(Config config) {
        vmName = config.getVmName();
        vmware = new VMware(config.getVMwareServerUser(), config.getVMwareServerPassword());
    }
    
    @Override
    public synchronized void start() throws IOException {
        vmware.start(vmName);        
    }
    
    @Override
    public synchronized void stop() throws IOException {        
        vmware.stop(vmName);        
    }

    @Override
    public boolean isAlive() throws IOException {
        boolean isRunning = false;
        for (String vm : vmware.getRunningVMs()) {
            if (vmName.equals(vm)) {
                isRunning = true;
                break;
            }
        }
        
        return isRunning;
    }
}

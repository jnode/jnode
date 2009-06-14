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
import java.util.List;

/**
 * Interface for a manager that handles a specific kind of VM (vmware, kvm, ...).
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public interface VmManager<T extends VMConfig> {
    /**
     * Starts the VM given by its name. 
     * @param config the configuration parameters for the VM.
     * @return true on success
     * @throws IOException
     */
    boolean start(T config) throws IOException;
    
    /**
     * Stops the VM given by its name.
     * 
     * @param vm name of the vm to stop
     * @param config the configuration parameters for the VM.
     * @return true on success
     * @throws IOException
     */
    boolean stop(T config) throws IOException;

    /**
     * Get the names of the currently running VMs.
     * 
     * @param config the configuration parameters for the VM.
     * @return list of VM names
     * @throws IOException
     */
    List<String> getRunningVMs(T config) throws IOException;
}

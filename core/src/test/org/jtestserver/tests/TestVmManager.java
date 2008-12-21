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
package org.jtestserver.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.jtestserver.client.process.VmManager;
import org.junit.Assert;
import org.junit.Test;

public abstract class TestVmManager {
    protected String vmName;

    protected VmManager vmManager;
    
    @Test    
    public void testGetRunningVMs() throws IOException {
        List<String> vms = vmManager.getRunningVMs();
        assertNotNull(vms);
    }
    
    @Test    
    public void testStartStop() throws IOException {
        List<String> vms = vmManager.getRunningVMs();
        final int initialNbVMs = vms.size();
        
        // start
        boolean success = vmManager.start(vmName);        
        assertTrue("start must work", success);
        
        vms = vmManager.getRunningVMs();
        Assert.assertTrue("list of running VMs must contains " + vmName, vms.contains(vmName));
        Assert.assertEquals("wrong number of running VMs", initialNbVMs + 1, vms.size());
        
        // stop
        success = vmManager.stop(vmName);        
        assertTrue("stop must work", success);
        
        vms = vmManager.getRunningVMs();
        Assert.assertFalse("list of running VMs must not contains " + vmName, vms.contains(vmName));
        Assert.assertEquals("wrong number of running VMs", initialNbVMs, vms.size());
    }
}

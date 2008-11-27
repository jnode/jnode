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

import org.jtestserver.client.Config;
import org.jtestserver.client.process.VMware;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestVMware {
    private String vmName;

    private VMware vmware;
    
    @Before
    public void setUp() throws IOException {
        Config config = Config.read();
        vmware = new VMware(config.getVMwareServerUser(), config.getVMwareServerPassword());
        vmName  = config.getVmName();
    }
    
    @Test    
    public void testGetRunningVMs() throws IOException {
        List<String> vms = vmware.getRunningVMs();
        assertNotNull(vms);
    }
    
    @Test(expected = IOException.class)    
    public void testGetRunningVMsWithWrongAuthentification() throws IOException {
        VMware vmware = new VMware("anObviouslyWrongLogin", "ThisIsNotAValidPassword");
        vmware.getRunningVMs();
    }
    
    @Test    
    public void testStartStop() throws IOException {
        List<String> vms = vmware.getRunningVMs();
        final int initialNbVMs = vms.size();
        
        // start
        boolean success = vmware.start(vmName);        
        assertTrue("start must work", success);
        
        vms = vmware.getRunningVMs();
        Assert.assertTrue("list of running VMs must contains " + vmName, vms.contains(vmName));
        Assert.assertEquals("wrong number of running VMs", initialNbVMs + 1, vms.size());
        
        // stop
        success = vmware.stop(vmName);        
        assertTrue("stop must work", success);
        
        vms = vmware.getRunningVMs();
        Assert.assertFalse("list of running VMs must not contains " + vmName, vms.contains(vmName));
        Assert.assertEquals("wrong number of running VMs", initialNbVMs, vms.size());
    }
}

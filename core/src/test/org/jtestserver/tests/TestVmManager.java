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

import org.jtestserver.client.process.VMConfig;
import org.jtestserver.client.process.VmManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public abstract class TestVmManager<T extends VMConfig> {
    protected T config;

    protected VmManager<T> vmManager;
    
    @After
    public void tearDown() {
        try {
            vmManager.stop(config);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    @Test    
    public void testGetRunningVMs() throws IOException {
        List<String> vms = vmManager.getRunningVMs(config);
        assertNotNull(vms);
    }
    
    @Test    
    public void testStartStop() throws IOException {
        ensureNoRunningVMs();
        
        List<String> vms = getRunningVMs(config);
        final int initialNbVMs = vms.size();
        Assert.assertTrue("No VM should be running", vms.isEmpty());
        
        // start
        boolean success = vmManager.start(config);
        assertTrue("start must work", success);
                
        vms = getRunningVMs(config);
        Assert.assertTrue("list of running VMs must contains '" + config.getVmName()
                + "'", vms.contains(config.getVmName()));
        Assert.assertEquals("wrong number of running VMs", initialNbVMs + 1, vms.size());
        
        // stop
        success = vmManager.stop(config);        
        assertTrue("stop must work", success);
        
        vms = getRunningVMs(config);
        Assert.assertFalse("list of running VMs must not contains '"
                + config.getVmName() + "'", vms.contains(config.getVmName()));
        Assert.assertEquals("wrong number of running VMs", initialNbVMs, vms.size());
    }
    
    private void ensureNoRunningVMs() throws IOException {
        List<String> vms;
        do {
            vmManager.stop(config);
            
            vms = getRunningVMs(config);
        } while (!vms.isEmpty());
    }
    
    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }
    
    private List<String> getRunningVMs(T config) throws IOException {
        sleep(1);
        return vmManager.getRunningVMs(config);
    }
}

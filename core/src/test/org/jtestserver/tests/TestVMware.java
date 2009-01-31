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

import java.io.IOException;
import java.util.Properties;

import org.jtestserver.client.Config;
import org.jtestserver.client.ConfigReader;
import org.jtestserver.client.process.VMConfig;
import org.jtestserver.client.process.vmware.VMware;
import org.jtestserver.client.process.vmware.VMwareConfig;
import org.junit.Before;
import org.junit.Test;

public class TestVMware extends TestVmManager {
    @Before
    public void setUp() throws IOException {
        Config config = new CustomConfigReader(ConfigReader.VMWARE_TYPE).readConfig();
        VMwareConfig vmwareConfig = (VMwareConfig) config.getVMConfig();

        vmManager = new VMware(vmwareConfig);
        vmName = vmwareConfig.getVmName();
    }

    @Test(expected = IOException.class)
    public void testGetRunningVMsWithWrongAuthentification() throws IOException {
        CustomConfigReader reader = new CustomConfigReader(ConfigReader.VMWARE_TYPE) {
            @Override
            protected VMConfig createVMConfig(Properties vmProperties, String vm)
                throws IOException {
                vmProperties.put(VMwareConfig.USERNAME, "anObviouslyWrongLogin");
                vmProperties.put(VMwareConfig.PASSWORD, "ThisIsNotAValidPassword");

                return super.createVMConfig(vmProperties, vm);
            }
        };

        Config config = reader.readConfig();
        VMwareConfig vmwareConfig = (VMwareConfig) config.getVMConfig();

        VMware vmware = new VMware(vmwareConfig);
        vmware.getRunningVMs();
    }
}

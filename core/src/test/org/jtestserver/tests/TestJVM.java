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

import org.jtestserver.client.Config;
import org.jtestserver.client.ConfigReader;
import org.jtestserver.client.process.jvm.JVM;
import org.jtestserver.client.process.jvm.JVMConfig;
import org.junit.Before;

public class TestJVM extends TestVmManager<JVMConfig> {
    @Before
    public void setUp() throws IOException {
        Config config = new CustomConfigReader(ConfigReader.JVM_TYPE).read(AllTests.CONFIG_DIRECTORY);
        this.config = (JVMConfig) config.getVMConfig();
        vmManager = new JVM();
    }
}

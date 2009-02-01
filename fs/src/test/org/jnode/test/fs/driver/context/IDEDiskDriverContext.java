/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.test.fs.driver.context;

import org.jmock.MockObjectTestCase;
import org.jnode.driver.Device;
import org.jnode.driver.block.ide.disk.IDEDiskDriver;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDriverUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.system.ResourceManager;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.fs.driver.BlockDeviceAPITestConfig;
import org.jnode.test.fs.driver.factories.MockIDEDeviceFactory;
import org.jnode.test.support.MockObjectFactory;
import org.jnode.test.support.TestConfig;

public class IDEDiskDriverContext extends BlockDeviceAPIContext {
    public IDEDiskDriverContext() {
        super("IDEDiskDriver");
    }

    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception {
        super.init(config, testCase);

        IDEDiskDriver driver = new IDEDiskDriver();

        // set the current testCase for our factory
        MockIDEDeviceFactory factory = (MockIDEDeviceFactory)
            IDEDriverUtils.getIDEDeviceFactory();
        factory.setTestCase(testCase);

        // create stub resource manager
        MockObjectFactory.createResourceManager(testCase);

        // create stub IDE device
        Device parent = MockObjectFactory.createParentDevice();
        BlockDeviceAPITestConfig cfg = (BlockDeviceAPITestConfig) config;
        IDEDevice device = MockObjectFactory.createIDEDevice(parent, testCase, true, cfg.getDeviceSize());

        init(null, driver, device);
    }

    protected void destroyImpl() {
        InitialNaming.unbind(ResourceManager.NAME);
    }
}

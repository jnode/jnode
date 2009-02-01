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
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.driver.block.ramdisk.RamDiskDriver;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.fs.driver.BlockDeviceAPITestConfig;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.test.support.TestConfig;

public class RamDiskDriverContext extends BlockDeviceAPIContext {
    public RamDiskDriverContext() {
        super("RamDiskDriver");
    }

    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception {
        super.init(config, testCase);

        BlockDeviceAPITestConfig cfg = (BlockDeviceAPITestConfig) config;
        String name = "RamDiskDevice-Tests";
        final RamDiskDevice device =
            new RamDiskDevice(StubDeviceManager.INSTANCE.getSystemBus(), name, cfg.getDeviceSize());
        final RamDiskDriver driver = new RamDiskDriver(name);
        init(null, driver, device);
    }
}

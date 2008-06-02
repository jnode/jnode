/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.test.fs.filesystem;

import org.jmock.MockObjectTestCase;
import org.jnode.driver.Device;
import org.jnode.test.fs.filesystem.config.DeviceParam;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.support.Context;
import org.jnode.test.support.TestConfig;

public class FSContext extends Context {
    private Device workDevice;
    private DeviceParam deviceParam;

    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception {
        FSTestConfig cfg = (FSTestConfig) config;
        deviceParam = cfg.getDeviceParam();
        workDevice = deviceParam.createDevice();

        cfg.getFileSystem().mount(workDevice);
    }

    public void destroy() throws Exception {
        deviceParam.tearDown(workDevice);
    }

    public Device getWorkDevice() {
        return workDevice;
    }
}

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

import java.io.IOException;
import javax.naming.NamingException;
import org.jmock.MockObjectTestCase;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.block.floppy.FloppyControllerFinder;
import org.jnode.driver.block.floppy.FloppyDeviceToDriverMapper;
import org.jnode.driver.block.floppy.FloppyDriver;
import org.jnode.driver.block.floppy.support.FloppyDriverUtils;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.fs.driver.factories.MockFloppyDeviceFactory;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.test.support.TestConfig;

public class FloppyDriverContext extends BlockDeviceAPIContext {
    public FloppyDriverContext() {
        super("FloppyDriver");
    }

    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception {
        super.init(config, testCase);

        // set the current testCase for our factory
        MockFloppyDeviceFactory factory;
        try {
            factory = (MockFloppyDeviceFactory)
                FloppyDriverUtils.getFloppyDeviceFactory();
        } catch (NamingException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
        factory.setTestCase(testCase);

        DeviceFinder deviceFinder = new FloppyControllerFinder();

        StubDeviceManager.INSTANCE.removeAll();
        StubDeviceManager.INSTANCE.add(deviceFinder, new FloppyDeviceToDriverMapper());

        FloppyDriver driver = (FloppyDriver) findDriver(deviceFinder, "fd0");
        log.debug("findDriver->" + driver);
        init(null, driver, null);
    }
}

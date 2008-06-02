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

package org.jnode.test.fs.driver.context;

import java.io.File;
import java.io.IOException;
import org.jmock.MockObjectTestCase;
import org.jnode.driver.block.FileDevice;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.support.TestConfig;
import org.jnode.test.support.TestUtils;

public class FileDeviceContext extends BlockDeviceAPIContext {
    final private File f;

    public FileDeviceContext() throws IOException {
        super("FileDevice");
        f = TestUtils.makeTempFile("TestFileDevice", "1M");
    }

    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception {
        super.init(config, testCase);

        FileDevice device = new FileDevice(f, "rw");
        init(null, device, null);
    }

    protected void destroyImpl() {
        f.delete();
    }
}

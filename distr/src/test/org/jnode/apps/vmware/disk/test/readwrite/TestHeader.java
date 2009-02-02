/*
 * $Id$
 *
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
 
package org.jnode.apps.vmware.disk.test.readwrite;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.test.Utils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class TestHeader extends BaseReadWriteTest {
    private static final Logger LOG = Logger.getLogger(TestHeader.class);

    public TestHeader(File diskFile) throws IOException {
        super(diskFile);
    }

    @Test
    public void read() throws Exception {
        VMWareDisk disk = new VMWareDisk(diskFile);
        disk.flush();
    }

    @Test
    public void write() throws Exception {
        VMWareDisk disk = new VMWareDisk(diskFile);
        disk.flush();
    }

    @Test
    public void writeAndRead() throws Exception {
        Utils.DO_CLEAR = false;

        LOG.info("BEGIN writeAndRead");
        VMWareDisk disk = new VMWareDisk(diskFile);

        // write
        LOG.info("writeAndRead: writing...");
        disk.flush();

        // read
        LOG.info("writeAndRead: reading...");
        VMWareDisk disk2 = new VMWareDisk(diskFile);
        Assert.assertEquals("disk has different size", disk.getLength(), disk2.getLength());
        Assert.assertEquals("disk has different descriptor", disk.getDescriptor(), disk2
                .getDescriptor());
        disk2.flush();
        LOG.info("END   writeAndRead");
    }
}

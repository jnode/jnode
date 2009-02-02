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
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.test.Utils;
import org.jnode.test.AnnotationTest.Test;
import org.junit.Ignore;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class TestVMWareDisk extends BaseReadWriteTest {
    private static final Logger LOG = Logger.getLogger(TestVMWareDisk.class);

    public TestVMWareDisk(File diskFile) throws IOException {
        super(diskFile);
    }

    @Test
    public void read() throws Exception {
        VMWareDisk disk = new VMWareDisk(diskFile);

        ByteBuffer data = IOUtils.allocate(IOHandler.SECTOR_SIZE * 100);
        disk.read(0, data);

        Assert.assertEquals(toString() + ": buffer should be filled", 0, data.remaining());
    }

    @Test
    @Ignore
    public void write() throws Exception {
        VMWareDisk disk = new VMWareDisk(diskFile);

        ByteBuffer data = IOUtils.allocate(IOHandler.SECTOR_SIZE * 100);
        disk.write(0, data);

        Assert.assertEquals(toString() + ": buffer should be fully copied", 0, data.remaining());
    }

    @Test
    @Ignore
    public void writeAndRead() throws Exception {
        Utils.DO_CLEAR = false;

        LOG.info("BEGIN writeAndRead");
        VMWareDisk disk = new VMWareDisk(diskFile);

        // write
        LOG.info("writeAndRead: writing...");
        int size = IOHandler.SECTOR_SIZE * 100;
        ByteBuffer expectedData = IOUtils.allocate(size);
        for (int i = 0; i < (size / 4); i++) {
            expectedData.putInt(i);
        }
        expectedData.rewind();
        disk.write(0, expectedData);
        disk.flush();

        // read
        LOG.info("writeAndRead: reading...");
        VMWareDisk disk2 = new VMWareDisk(diskFile);
        Assert.assertEquals("disk has different size", disk.getLength(), disk2.getLength());
        Assert.assertEquals("disk has different descriptor", disk.getDescriptor(), disk2
                .getDescriptor());

        expectedData.rewind();
        ByteBuffer actualData = IOUtils.allocate(size);
        disk2.read(0, actualData);
        long nbErrors = 0;
        long nbInts = (size / 4);
        for (int i = 0; i < nbInts; i++) {
            int actual = actualData.getInt(i);
            int expected = expectedData.getInt();
            // Assert.assertEquals("bad data at index "+(i*4), expected,
            // actual);
            if (actual != expected) {
                nbErrors++;
            }
        }
        double ratio = ((double) ((10000 * nbErrors) / nbInts)) / 100.0;
        Assert.assertTrue("bad data. nbErrors=" + nbErrors + " (ratio=" + ratio + "%)",
                (nbErrors == 0));

        LOG.info("END   writeAndRead");
    }
}

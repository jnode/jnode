/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.fs.hfsplus;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.service.FileSystemService;
import org.jnode.test.support.TestUtils;

public class HfsPlusFileSystemTest extends TestCase {
    
    private Device device;
    private FileSystemService fss;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // create test device.
        device = createTestDisk(false);
        // create file system service.
        fss = FileSystemTestUtils.createFSService(HfsPlusFileSystemType.class.getName());
    }

    public void testReadSmallDisk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("hfsplus/test.hfsplus"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:Kenny total:67108864 free:66035712\n" +
            "  /; \n" +
            "    southpark.jpeg; 6420; 5a2ec290089ee04a470135f3bda29f94\n" +
            "    southpark.jpeg:rsrc; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    test.txt; 1141; 48b97c1f1defb52c77ce75d55a4b066c\n" +
            "    test.txt:rsrc; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    public void testCreate() throws Exception {
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = new HfsPlusFileSystem(device, false, type);
        HFSPlusParams params = new HFSPlusParams();
        params.setVolumeName("testdrive");
        params.setBlockSize(HFSPlusParams.OPTIMAL_BLOCK_SIZE);
        params.setJournaled(false);
        params.setJournalSize(HFSPlusParams.DEFAULT_JOURNAL_SIZE);
        fs.create(params);
        SuperBlock vh = fs.getVolumeHeader();
        assertEquals(SuperBlock.HFSPLUS_SUPER_MAGIC, vh.getMagic());
        assertEquals(4096, vh.getBlockSize());

    }

    public void testRead() throws Exception {
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = new HfsPlusFileSystem(device, false, type);
        HFSPlusParams params = new HFSPlusParams();
        params.setVolumeName("testdrive");
        params.setBlockSize(HFSPlusParams.OPTIMAL_BLOCK_SIZE);
        params.setJournaled(false);
        params.setJournalSize(HFSPlusParams.DEFAULT_JOURNAL_SIZE);
        fs.create(params);
        fs.close();
        fs = new HfsPlusFileSystemType().create(device, false);
        fs.read();
        fs.createRootEntry();
        FSDirectory root = fs.getRootEntry().getDirectory();
        assertFalse("Must be empty", root.iterator().hasNext());
        root.addDirectory("test");
        fs.flush();
        fs.close();
        fs = new HfsPlusFileSystemType().create(device, false);
        fs.read();
        assertEquals(1,fs.getVolumeHeader().getFolderCount());
        fs.createRootEntry();
        root = fs.getRootEntry().getDirectory();
        assertTrue("Must contains one directory", root.iterator().hasNext());
    }

    private Device createTestDisk(boolean formatted) throws IOException {
        File file = TestUtils.makeTempFile("hfsDevice", "10M");
        Device device = new FileDevice(file, "rw");
        return device;

    }
}

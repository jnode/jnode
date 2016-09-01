/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.test.fs.jfat;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.fs.jfat.FatFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.Before;
import org.junit.Test;

public class FatFileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(FatFileSystemType.class.getName());
    }

    @Test
    public void testReadFat32Disk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/jfat/test.fat32"), "r");
        FatFileSystemType type = fss.getFileSystemType(FatFileSystemType.ID);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: JFAT vol: total:-1 free:-1\n" +
                "  ; \n" +
                "    dir1; \n" +
                "      test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2\n" +
                "    dir2; \n" +
                "      test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7\n" +
                "    test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadFat16Disk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/jfat/test.fat16"), "r");
        FatFileSystemType type = fss.getFileSystemType(FatFileSystemType.ID);
        FatFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: JFAT vol: total:-1 free:-1\n" +
                "  ; \n" +
                "    dir1; \n" +
                "      test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2\n" +
                "    dir2; \n" +
                "      test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7\n" +
                "    test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}


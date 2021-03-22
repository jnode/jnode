/*
 * $Id$
 *
 * Copyright (C) 2003-2016 JNode.org
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

package org.jnode.test.fs.xfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.xfs.XfsFileSystem;
import org.jnode.fs.xfs.XfsFileSystemType;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link XfsFileSystem}.
 */
public class XfsFileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(XfsFileSystemType.class.getName());
    }

    @Test
    public void testRead() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/xfs/xfs.dd"), "r");
        XfsFileSystemType type = fss.getFileSystemType(XfsFileSystemType.ID);
        XfsFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: XFS vol:null total:0 free:0\n" +
            "  /; \n" +
            "    Respect.jpg; 15277; c044bc1808d00fce44c8b7fa75d347df\n" +
            "    info; \n" +
            "      failed_debian_boot.jpg; 416928; 269ba81ff51ab6686ab97dbdcceb3d03\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}

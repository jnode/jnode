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
 
package org.jnode.test.fs.ext4;

import java.util.Iterator;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ext2.Ext2Entry;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemType;
import org.jnode.fs.ext2.xattr.XAttrEntry;
import org.jnode.fs.service.FileSystemService;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Ext4FileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(Ext2FileSystemType.class.getName());
    }

    @Test
    public void testReadExt4SpecialFiles() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/test-special-files.ext4"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:15728640 free:13918208\n" +
                "  /; \n" +
                "    lost+found; \n" +
                "    console; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    fifo; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    sda1; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    The_Rabies_Virus_Remains_a_Medical_Mystery.jpg; 6606; a28d342db2d2081f9d2eb287d49c1110\n" +
                "    wired-science.jpg; 16; 67bc4bf64a29239a9f148fb768bfbbc8\n" +
                "    wolf_slice_1.jpg; 6606; a28d342db2d2081f9d2eb287d49c1110\n" +
                "    index.html; 106102; 99248bc850c65b03b04776342e4b3e7d\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadExt4Mmp() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-mmp.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:2998272 free:1870848\n" +
                "  /; \n" +
                "    lost+found; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadExt4LargeDirectory() throws Exception {

        // Filesystem created without the 'dir_index' feature
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-large-directory.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        FSDirectory rootDirectory = fs.getRootEntry().getDirectory();
        FSDirectory largeDirectory = rootDirectory.getEntry("large-directory").getDirectory();

        int childCount = 0;
        Iterator<? extends FSEntry> iterator = largeDirectory.iterator();
        while (iterator.hasNext()) {
            FSEntry entry = iterator.next();

            if (entry.isFile()) {
                Assert.assertEquals("b1946ac92492d2347c6235b4d2611184", DataStructureAsserts.getMD5Digest(entry.getFile()));
                childCount++;
            }
        }

        Assert.assertEquals(65001, childCount);
    }

    @Test
    public void testReadExt4LargeDirectoryWithIndex() throws Exception {

        // Filesystem created with the 'dir_index' feature
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-large-dir-with-index.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        FSDirectory rootDirectory = fs.getRootEntry().getDirectory();
        FSDirectory largeDirectory = rootDirectory.getEntry("large-directory").getDirectory();

        int childCount = 0;
        Iterator<? extends FSEntry> iterator = largeDirectory.iterator();
        while (iterator.hasNext()) {
            FSEntry entry = iterator.next();

            if (entry.isFile()) {
                Assert.assertEquals("b1946ac92492d2347c6235b4d2611184", DataStructureAsserts.getMD5Digest(entry.getFile()));
                childCount++;
            }
        }

        Assert.assertEquals(65001, childCount);
    }

    @Test
    public void testReadExt4FlexBG() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-flex-bg.img"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol:ext4-ftw-omgz total:127926272 free:123949056\n" +
            "  /; \n" +
            "    lost+found; \n" +
            "    wolf_slice_1.jpg; 6606; a28d342db2d2081f9d2eb287d49c1110\n" +
            "    wired-science.jpg; 16; 67bc4bf64a29239a9f148fb768bfbbc8\n" +
            "    The_Rabies_Virus_Remains_a_Medical_Mystery.jpg; 6606; a28d342db2d2081f9d2eb287d49c1110\n" +
            "    console; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    sda1; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    fifo; 0; d41d8cd98f00b204e9800998ecf8427e\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadExt4MetaBG() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-meta-bg.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:4997120 free:3680256\n" +
                "  /; \n" +
                "    lost+found; \n" +
                "    Fairy-Penguin.jpg; 60472; 78da81a8cf672de95d27214d44a5ea59\n" +
                "    why.jpg; 30965; 9b82ac413bb4204a4cf6d3e801af38fd\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadExt4M64Bit() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-64bit.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol:VDI-FTW total:9437184 free:7313408\n" +
            "  /; \n" +
            "    lost+found; \n" +
            "    COPYING; 18693; d7810fab7487fb0aad327b76f1be7cd7\n" +
            "    CREDITS; 98277; 19f7db100be5bc0bf2b13877667bc0bb\n" +
            "    Kbuild; 2888; f70ebd21ac6daa2a7adfd0d1b382726a\n" +
            "    Kconfig; 252; 2370b55729048373f46fb6367e2e3dba\n" +
            "    MAINTAINERS; 382615; a38d5e144061abb5fe67bfba3e73ec81\n" +
            "    Makefile; 59015; 3069a09ff10793d0e05f12503a495828\n" +
            "    README; 18372; 50a2a143e6733ed6dadde20d94b31109\n" +
            "    REPORTING-BUGS; 7490; 066e4510a216b82a632a6c5d4c67e8d9\n" +
            "    test; \n" +
            "      .bashrc; 3771; 1f98b8f3f3c8f8927eca945d59dcc1c6\n" +
            "      remote-region.sh; 345; 7d9e35dd5f8467f652c99236da8df412\n" +
            "      .xsession-errors; 3691; a8cdad2838879b61e739db5dec9d9c1a\n" +
            "      Makefile; 9830; 115b7ff3e0a550f90923db7c748da7f9\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);

        assertThat(fs.getSuperblock().getBlocksCount(), is(9216L));
    }

    @Test
    public void testInlineData() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/inline.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:4194304 free:2866176\n" +
            "  /; \n" +
            "    lost+found; \n" +
            "    inline.txt; 31; 85da5521483449f0687b626cb376e8f8\n" +
            "    small-dir; \n" +
            "      peanut-butter.txt; 80; c9d85be191c94f8605bd9f798d6c2354\n" +
            "    xattr.txt; 90; 84cddc714c783ed1544fba5433328c08\n" +
            "    tiny-dir; \n" +
            "      a; 0; d41d8cd98f00b204e9800998ecf8427e\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testSelinuxXattr() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ext4/ext4-selinux.dd"), "r");
        Ext2FileSystemType type = fss.getFileSystemType(Ext2FileSystemType.ID);
        Ext2FileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: EXT2 vol: total:98304 free:76800\n" +
                "  /; \n" +
                "    lost+found; \n" +
                "    foo; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    1; 3; acbd18db4cc2f85cedef654fccc4a4d8\n" +
                "    2; 3; acbd18db4cc2f85cedef654fccc4a4d8\n" +
                "    3; 3; acbd18db4cc2f85cedef654fccc4a4d8\n" +
                "    4; 3; acbd18db4cc2f85cedef654fccc4a4d8\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);

        Ext2Entry fileEntry = (Ext2Entry) fs.getRootEntry().getDirectory().getEntry("1");
        XAttrEntry selinuxAttribute = fileEntry.getINode().getAttribute("selinux");
        assertThat(new String(selinuxAttribute.getValue(), "US-ASCII"), is("system_u:object_r:unlabeled_t\0"));
    }
}


package org.jnode.fs.ext4;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.junit.Before;
import org.junit.Test;

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

        device = new FileDevice(FileSystemTestUtils.getTestFile("ext4/test-special-files.ext4"), "r");
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
}


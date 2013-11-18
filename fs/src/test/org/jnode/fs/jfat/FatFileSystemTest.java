package org.jnode.fs.jfat;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.service.FileSystemService;
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

        device = new FileDevice(FileSystemTestUtils.getTestFile("jfat/test.fat32"), "r");
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

        device = new FileDevice(FileSystemTestUtils.getTestFile("jfat/test.fat16"), "r");
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


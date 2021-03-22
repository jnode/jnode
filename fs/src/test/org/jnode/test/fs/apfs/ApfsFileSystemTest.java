package org.jnode.test.fs.apfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.apfs.ApfsFileSystem;
import org.jnode.fs.apfs.ApfsFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.Before;
import org.junit.Test;

public class ApfsFileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(ApfsFileSystemType.class.getName());
    }

    @Test
    public void testSimpleImage() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/apfs/simple.dd"), "r");
        ApfsFileSystemType type = fss.getFileSystemType(ApfsFileSystemType.ID);
        ApfsFileSystem fs = type.create(device, true);

        String expectedStructure =
            "";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}


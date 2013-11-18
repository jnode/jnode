package org.jnode.fs.ntfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.service.FileSystemService;
import org.junit.Before;
import org.junit.Test;

public class NTFSFileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(NTFSFileSystemType.class.getName());
    }

    @Test
    public void testReadSmallDisk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("ntfs/test.ntfs"), "r");
        NTFSFileSystemType type = fss.getFileSystemType(NTFSFileSystemType.ID);
        NTFSFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: NTFS vol: total:104857600 free:102283264\n" +
            "  .; \n" +
            "    $AttrDef; 2560; ad617ac3906958de35eacc3d90d31043\n" +
            "    $BadClus; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    $BadClus:$Bad; 104857088; f85075a81e3af0e4d0896594e3ecf54e\n" +
            "    $Bitmap; 25600; ed326910f779c1a038bb9344410d93f4\n" +
            "    $Boot; 8192; 66d2b7de4671946357039f6ac5f3646b\n" +
            "    $Extend; \n" +
            "      $ObjId; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "      $Quota; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "      $Reparse; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    $LogFile; 2097152; d742d2de9f201cd58e9b8642d683c18c\n" +
            "    $MFT; 32768; 97279ad7f93ee3c54dc079b91d688fbf\n" +
            "    $MFTMirr; 4096; d2aea7f2c408e32cf3bf718427f8006f\n" +
            "    $Secure; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    $Secure:$SDS; 262928; 903d478f933c7cdcc9064f4fab22b6f2\n" +
            "    $UpCase; 131072; 6fa3db2468275286210751e869d36373\n" +
            "    $Volume; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    dir1; \n" +
            "      test.txt; 18; 80aeb09eb86de4c4a7d1f877451dc2a2\n" +
            "    dir2; \n" +
            "      test.txt; 18; 1b20f937ce4a3e9241cc907086169ad7\n" +
            "    test.txt; 18; fd99fcfc86ba71118bd64c2d9f4b54a4\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}

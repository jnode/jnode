/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.test.fs.hfsplus;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusEntry;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusFileSystemType;
import org.jnode.fs.hfsplus.SuperBlock;
import org.jnode.fs.hfsplus.attributes.AttributeData;
import org.jnode.fs.service.FileSystemService;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.jnode.test.support.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HfsPlusFileSystemTest {

    private Device device;
    private FileSystemService fss;

    @Before
    public void setUp() throws Exception {
        // create test device.
        device = createTestDisk(false);
        // create file system service.
        fss = FileSystemTestUtils.createFSService(HfsPlusFileSystemType.class.getName());
    }

    @Test
    public void testReadSmallDisk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/test.hfsplus"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:Kenny total:67108864 free:66035712\n" +
            "  /; \n" +
            "    southpark.jpeg; 6420; 5a2ec290089ee04a470135f3bda29f94\n" +
            "    test.txt; 1141; 48b97c1f1defb52c77ce75d55a4b066c\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadDiskWithDirectoryHardLinks() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/hard-linked-directories.dmg"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:Hard linked directories total:40960000 free:39428096\n" +
            "  /; \n" +
            "    .DS_Store; 6148; cbdca44c18b8de8671b413b2023ef664\n" +
            "    .fseventsd; \n" +
            "      00000000214ea109; 231; 11618d6f301d3672e498609838d23a8c\n" +
            "      00000000214ea10a; 72; 6f20b722869a82510ca98e99071c4aca\n" +
            "      fseventsd-uuid; 36; 2b95938d530cb32e96dfc01671095522\n" +
            "    .HFS+ Private Directory Data\r; \n" +
            "      dir_25; \n" +
            "        file.txt; 38; 23c1bd7263b9abbdbb879e6267d84ff8\n" +
            "    .journal; 524288; 7c1d0a50a9738dd88572a9cee56c0270\n" +
            "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
            "    .Trashes; \n" +
            "    dir1; \n" +
            "      clone; \n" +
            "        file.txt; 38; 23c1bd7263b9abbdbb879e6267d84ff8\n" +
            "    dir2; \n" +
            "      clone; \n" +
            "        file.txt; 38; 23c1bd7263b9abbdbb879e6267d84ff8\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadDiskWithFileHardLinks() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/hard-linked-files.dmg"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:hard-links total:40960000 free:39436288\n" +
                "  /; \n" +
                "    .DS_Store; 6148; b5ae7323596898677123c65fcce1be07\n" +
                "    .fseventsd; \n" +
                "      00000000214eb5f7; 121; 45f770b87c4fb7773466ed4ea7333248\n" +
                "      00000000214eb5f8; 72; 261dba091a629e61f127ed183b42ae01\n" +
                "      fseventsd-uuid; 36; f4f9aca6866b93ba4ab04768132dbbf6\n" +
                "    .HFS+ Private Directory Data\r; \n" +
                "    .journal; 524288; 7d69775e76f5a59e0f8687f792df23dc\n" +
                "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
                "    .Trashes; \n" +
                "    arrest.txt; 1933; bedea6f1277f61a924388fbb58281e4a\n" +
                "    diapers.txt; 1933; bedea6f1277f61a924388fbb58281e4a\n" +
                "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n" +
                "      iNode27; 1933; bedea6f1277f61a924388fbb58281e4a\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadDiskWithCompressedFiles() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/compressed.dd"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:Disk Image total:102379520 free:99401728\n" +
            "  /; \n" +
            "    .DS_Store; 6148; 98cf7ff1fd8e81ba4839043e208fb63e\n" +
            "    .fseventsd; \n" +
            "      00000000219733ae; 158; f512a16c903a637e27f3e81a10f224a2\n" +
            "      00000000219733af; 72; fa4d4f58441685c90841126fb5ea35e5\n" +
            "      fseventsd-uuid; 36; 3495348c1edb3f39b3cd4222024723c0\n" +
            "    .HFS+ Private Directory Data\r; \n" +
            "    .journal; 524288; ba95a916b83c8478fb22c180893cffff\n" +
            "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
            "    .Trashes; \n" +
            "    compression.html; 81757; 64ae4e5007fdec27518d9073c72a1714\n" +
            "    compression.html:rsrc; 21007; 6727899de0b20d7dcb92c68ecaa8bfe2\n" +
            "    small.txt; 6; d15dbfcb847653913855e21370d83af1\n" +
            "    zlib.txt; 6211; e63c9b3344d96dbd6c5ddd0debde06f0\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskWithIncorrectCompressedFileOnFile() throws Exception {
        // This HFS+ image was created under Linux and it seems like the 'quote.txt' file has the UF_COMPRESSED
        // flag set on it incorrectly
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/wrong-compressed-flag.dd"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:untitled total:2097152 free:1904640\n" +
            "  /; \n" +
            "    quote.txt; 165; 357d31c02f4b9161d14182b57769ef7a\n" +
            "    steve-jobs-holding-iphone.jpg; 107795; 17baa8a85e36a790df697a68362c227d\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskAttributes() throws Exception {
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/attributes.dd"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:attributes total:99983360 free:97062912\n" +
            "  /; \n" +
            "    .DS_Store; 6148; 7e2a612ff5e605e73b2078557c9aa5c5\n" +
            "    .fseventsd; \n" +
            "      0000000000219f17; 135; b518731969693d10590124b27c8b50cf\n" +
            "      0000000000219f18; 70; e899edc609e2d4bac697bc207b7ee5ad\n" +
            "      0000000000219f19; 38; ebb12e59f60c213f883d07e17ff3f2d5\n" +
            "      0000000000219f1a; 71; 4732d018d8011a207fce3f127a2e6e5d\n" +
            "      0000000000219f1b; 38; ebb12e59f60c213f883d07e17ff3f2d5\n" +
            "      0000000000219f1c; 71; b619c8d189af0a632e91dec707774d44\n" +
            "      fseventsd-uuid; 36; 8f91e1c548e1eed2edb44694d8a5c5db\n" +
            "    .HFS+ Private Directory Data\r; \n" +
            "    .journal; 524288; 3e87e55c8d321c611f18197770a523e7\n" +
            "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
            "    isoparser-1.1.22.pom; 6652; 6c04a5fb9540f1b558dc9465ab3a7ad4\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);

        HfsPlusEntry entry = (HfsPlusEntry) fs.getRootEntry().getDirectory().getEntry("isoparser-1.1.22.pom");
        HfsPlusFile file = (HfsPlusFile) entry.getFile();

        List<String> attributes = fs.getAttributes().getAllAttributes(file.getCatalogFile().getFileId());
        AttributeData whereFromAttr = fs.getAttributes().getAttribute(file.getCatalogFile().getFileId(),
            "com.apple.metadata:kMDItemWhereFroms");
        ByteBuffer whereFromBuffer = ByteBuffer.allocate(6);
        whereFromAttr.read(fs, 0, whereFromBuffer);
        String whereFrom = new String(whereFromBuffer.array(), "UTF-8");

        AttributeData quarantineAttr = fs.getAttributes().getAttribute(file.getCatalogFile().getFileId(),
            "com.apple.quarantine");
        ByteBuffer quarantineBuffer = ByteBuffer.allocate((int) quarantineAttr.getSize());
        quarantineAttr.read(fs, 0, quarantineBuffer);
        String quarantine = new String(quarantineBuffer.array(), "UTF-8");

        Assert.assertEquals("[com.apple.metadata:kMDItemWhereFroms, com.apple.quarantine]", attributes.toString());
        Assert.assertEquals(144, whereFromAttr.getSize());
        Assert.assertEquals("bplist", whereFrom);
        Assert.assertEquals("0081;5cc8ba96;Chrome;3792716B-4DE2-4A43-BFA3-516714DD5764", quarantine);
    }

    @Test
    public void testDiskCompressedHardlinks() throws Exception {
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/compressed-hardlinks.dd"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:Will it work? total:40960000 free:39424000\n" +
            "  /; \n" +
            "    .fseventsd; \n" +
            "      0000000021b70ddc; 134; c4bd63b946eb863f50b189f2cb253c8c\n" +
            "      0000000021b70ddd; 72; 3bf09d08a28b8988cec8f4e3c166ee96\n" +
            "      fseventsd-uuid; 36; 518c962c5c2852fd354b18650e198372\n" +
            "    .HFS+ Private Directory Data\r; \n" +
            "    .journal; 524288; b324e1aae290bc30297418b2c39cefa3\n" +
            "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
            "    .Trashes; \n" +
            "    coffee-again.txt; 2573; 3a66504af332c4e6d9997e52cce98002\n" +
            "    coffee.txt; 2573; 3a66504af332c4e6d9997e52cce98002\n" +
            "    i-own-you.jpg; 24085; a1a91dfb9c2c0db6bec2f55b12a2e97f\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n" +
            "      iNode24; 2573; 3a66504af332c4e6d9997e52cce98002\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskWithLargeCompressedFile() throws Exception {
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/large-compressed.dmg"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:large compressed file total:40960000 free:39411712\n" +
            "  /; \n" +
            "    .fseventsd; \n" +
            "      00000000220a3c77; 96; 52a732bebb5103be73aa89617d42a747\n" +
            "      00000000220a3c78; 72; 54d11397f6d87faf48a42c82aa2df57d\n" +
            "      fseventsd-uuid; 36; 81d67b0f96aea5c2a1567a28dfa32fb7\n" +
            "    .HFS+ Private Directory Data\r; \n" +
            "    .journal; 524288; 43c347a01dd468234a75c8f5f126858e\n" +
            "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
            "    .Trashes; \n" +
            "    large-useless-text.txt; 122818; e33db0ee58f4f5413c721b3d99311215\n" +
            "    large-useless-text.txt:rsrc; 36771; fba9ba66e57abb9e691a6fb62fcd7c17\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskWithLzvnCompression() throws Exception {
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/hfsplus/rle-compression.dmg"), "r");
        HfsPlusFileSystemType type = fss.getFileSystemType(HfsPlusFileSystemType.ID);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: HFS+ vol:rle-compression total:40960000 free:39092224\n" +
            "  /; \n" +
            "    .fseventsd; \n" +
            "      000000000039ce79; 109; 151712afa50c634d4e796e06025b6779\n" +
            "      000000000039ce7a; 71; e659b5b401cb15e01fb9b5dd65b733ef\n" +
            "      fseventsd-uuid; 36; c2c77c331f977e1b274da06abc7c778a\n" +
            "    .HFS+ Private Directory Data\r; \n" +
            "    .journal; 524288; b7106768943c0ae7b09e84b5bf75e62f\n" +
            "    .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc\n" +
            "    .Trashes; \n" +
            "    bash; 628640; f81cce1751382506604e244039bf4724\n" +
            "    bash:rsrc; 352797; 699818770c06dc378bb2dd13ca159b33\n" +
            "    \u0000\u0000\u0000\u0000HFS+ Private Data; \n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
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
        Assert.assertEquals(SuperBlock.HFSPLUS_SUPER_MAGIC, vh.getMagic());
        Assert.assertEquals(4096, vh.getBlockSize());

    }

    @Test
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
        Assert.assertFalse("Must be empty", root.iterator().hasNext());
        root.addDirectory("test");
        fs.flush();
        fs.close();
        fs = new HfsPlusFileSystemType().create(device, false);
        fs.read();
        Assert.assertEquals(1, fs.getVolumeHeader().getFolderCount());
        fs.createRootEntry();
        root = fs.getRootEntry().getDirectory();
        Assert.assertTrue("Must contains one directory", root.iterator().hasNext());
    }

    private Device createTestDisk(boolean formatted) throws IOException {
        File file = TestUtils.makeTempFile("hfsDevice", "10M");
        Device device = new FileDevice(file, "rw");
        return device;

    }
}

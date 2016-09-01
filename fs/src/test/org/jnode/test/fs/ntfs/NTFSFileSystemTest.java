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
 
package org.jnode.test.fs.ntfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSFileSlackSpace;
import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSEntry;
import org.jnode.fs.ntfs.NTFSFileSystem;
import org.jnode.fs.ntfs.NTFSFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.Assert;
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

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ntfs/test.ntfs"), "r");
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

    @Test
    public void testReadCompressedDisk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ntfs/compressed.dd"), "r");
        NTFSFileSystemType type = fss.getFileSystemType(NTFSFileSystemType.ID);
        NTFSFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: NTFS vol:COMPRESS total:26214400 free:15031296\n" +
                "  .; \n" +
                "    OBJS2.PRS; 57439; 46d0c5fe8dd36c0f1ecc042a38188740\n" +
                "    PICTURES.DBF; 275; 18c0dceeb707590e8c8348e725494993\n" +
                "    SKELET4.DCX; 79770; 977402a527c66cb64958fb40cc01697f\n" +
                "    $AttrDef; 2560; ad617ac3906958de35eacc3d90d31043\n" +
                "    $BadClus; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    $BadClus:$Bad; 26213376; d2c55adc338f3da60e4ccb436894e118\n" +
                "    $Bitmap; 3200; 20788d6cf14badc3a56d4576951f97fd\n" +
                "    $Boot; 8192; 022889ceb3537c9a0b7a185b8954d704\n" +
                "    $Extend; \n" +
                "      $ObjId; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "      $Quota; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "      $Reparse; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "      $RmMetadata; \n" +
                "        $Repair; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "        $Repair:$Config; 8; 39d997ae6b77dab1dfbabb6cf0e4783a\n" +
                "        $Txf; \n" +
                "        $TxfLog; \n" +
                "          $Tops; 100; 8f7cd9b581039e54125c302fc088bc2c\n" +
                "          $Tops:$T; 1048576; b6d81b360a5672d80c27430f39153e2c\n" +
                "          $TxfLog.blf; 65536; 46085970c067c49a20a86beb4fefd976\n" +
                "          $TxfLogContainer00000000000000000001; 1048576; 334e61d6148946e93b5832015334d621\n" +
                "          $TxfLogContainer00000000000000000002; 1048576; b6d81b360a5672d80c27430f39153e2c\n" +
                "    $LogFile; 2097152; ac996b5f8a2dc8085802eb3c77f67e55\n" +
                "    $MFT; 262144; 5afc794069bef8d2c51888cbb74ad965\n" +
                "    $MFTMirr; 4096; 1362cebe0d7c88016c73c7bdda96bd78\n" +
                "    $Secure; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    $Secure:$SDS; 263068; 8131520660e0075df6b27e316f32918e\n" +
                "    $UpCase; 131072; 7ff498a44e45e77374cc7c962b1b92f2\n" +
                "    $Volume; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
                "    ARROW-22.WPG; 116; 2eb315951e81eb8bf23d57a65b625602\n" +
                "    CCD development meeting.cal; 214; fd7c111a9e2bdd519ce42cd51d084d30\n" +
                "    Compressed; \n" +
                "      LAYERS.AI; 56985; f96800fbf0f8f43c5a63a29c4b5188b9\n" +
                "      mab.pbm; 50568; 9b35f41bb32cfc2634721d02ac1b74b6\n" +
                "      mdiframe.ico; 766; ebffbc2e89ff705b68f22ca742b979a0\n" +
                "      axis.vsd; 45568; 1641c6e438e258278db49fa3d39b2ce1\n" +
                "      Bar.xls; 22016; 3a6140c9b8bbecf6455712b06287711e\n" +
                "      BIRDS.RAS; 47842; 0505c0df790006af9c686555acb6a266\n" +
                "      BlkDia.vsd; 51200; 21755ce45f620d371b36ac1fb4350386\n" +
                "      CHROMA.DXF; 41221; 7e5bc5ef74e2442cec50bebeeb671f57\n" +
                "      complex.lwp; 48052; ec951c4c69b2f38f68d21163311bf52d\n" +
                "      Creativity.ppt; 53248; 68b47baabfab01f399b1228f3a4d1083\n" +
                "      Dataflow Diagram.VSD; 51712; 6ced4c9167cb5e785ccc0900aa7d6927\n" +
                "      desktop.ini; 46; 15478b340a8362bb79fd2a6ea0dde1a0\n" +
                "      Fishbone Diagram.VSD; 50176; 81bdc683508083697f2751dc51f093ed\n" +
                "      GRPHBORD.DOC; 46592; b5f6355c028ce9bb26aa0b1dc2428940\n" +
                "      ICESTORM.PCD; 1121280; d6d479ae1f356cf4f6d9058142b1fc26\n" +
                "      InsertDiagram2.doc; 42496; 3b2fde2a9fc3cfdb9f079ca281e8c410\n" +
                "      J_Word97_6.DOC; 19456; 6aa61bd20d1d93afa591af86b6abb224\n" +
                "      Koala52.wbmp; 45855; aacf37e664f03f34b1a920d134fd01bb\n" +
                "      LABEL.WDB; 5120; 583de9a6003d8411c5b43180dc4c5fd6\n" +
                "      leaf_BG.JPG; 3781; 63a3c7d05f61b7efada843a86e3003aa\n" +
                "      LEAR.PDF; 290205; 0b72ec9e79c7a8a93d9cd318a2ff0b84\n" +
                "      LEGACY.CHP; 44114; a39473df4757643c41967fb2ab042262\n" +
                "      LESSON2.HWP; 26280; fd230e8ee10d319aa8fe4247eb050964\n" +
                "      LESSON5.WQ2; 7258; 2ad524284554c51ebf8a92aff9bf0f86\n" +
                "      LETTER.WPS; 2345; d3fd0caea04de8f93920d5ab81e8fcfc\n" +
                "      Letterhead; 205824; 3a811f935a2bab85127d1c0d3fffcf3a\n" +
                "      linecharts.xls; 37888; 0e713ed655141c8de84da6b73cfa9b0b\n" +
                "      linecust.vsd; 25088; 4d626b9d0ba4842297a1871a4203593e\n" +
                "      line_spacing.shw; 80896; 83faa0f674a1c6a7d781c765ed2b0abb\n" +
                "      Logo.gif; 8305; 69a534c977ce1dd5c45077dcc083cb8e\n" +
                "      lotsofsides.SHW; 78848; 889a6312c21063d8bec2d38f0bdc8eac\n" +
                "      LOTUSNAP.001; 106591; 3f18ddf66beedd03e5876f0209b32de4\n" +
                "      MACWRK2.DB; 2581; 2517d060c1c53f436ab46b43addad742\n" +
                "      MACXL4.XLS; 9828; f12b57d1c6597dedab300ccc0009aaa4\n" +
                "      MAKEVID.EXE; 8727; 0f66ae5a5205514f19f1f956bcedc153\n" +
                "      MANUSCRT.DOC; 5120; 2b2d13f4467ca6565e32760e6a046ea8\n" +
                "      MANY.XLS; 1520; 01249401f5524853a4685356268d1bde\n" +
                "      ManyFields2.msg; 15360; de3a19cab697c2f7094a5b38182796d7\n" +
                "      ManyFields3.msg; 15360; d4dc90d5a2f8f5eca6ee5a879839e4d1\n" +
                "      ManyFields4.msg; 17408; bfbee2f23b7a725c87f9e3fdc333414e\n" +
                "      ManyFields5.msg; 17408; 388e44b47eeb174e9c9b28390facc926\n" +
                "      MASK.AI; 64260; 04e84a978cfeffcb13ad86f915941082\n" +
                "      MATH.DOX; 3072; 501cca369ce2c2824184e940734e5b88\n" +
                "      MAXCOL2.RBF; 3236; 251c5883485c9833cdef0c1faaf4f524\n" +
                "      mergedataerase2.xls; 13824; 10c75bf5068a776e650767fa30099247\n" +
                "      MINCOL2.RBF; 502; 4ec284022ac9b646d6f4e83dbc2d5503\n" +
                "      miyazawaKenji.jtt; 32256; 798bf01407741fd4ae818f906ef316b2\n" +
                "      moon.sdw; 658; d26cc260a1ab66dd5cafdba023513297\n" +
                "      Mountain Lake.psp; 1393265; 6fc89b022055869be56ca919faef1f4d\n" +
                "      MPLAN4.MOD; 25130; 584d47aacd355fa0a202587c33aaa664\n" +
                "      MSBUG2.MIF; 195628; f2c166c0f2decbc2ac556b1d71c5490e\n" +
                "      MSRTF.RTF; 5725; 5bcf974a1152546cda39bbb1af8919cf\n" +
                "      MTRCYCLE.TGA; 307986; 9d02d2694a0267444a07d1e3c718ba29\n" +
                "      Naming.wpd; 6322; 4133b40c1385e69da2dbe9341caacdeb\n" +
                "      NARROW.WRI; 14464; 811ac4dcf21aab1c3db9852f53b36a04\n" +
                "      NAVYDIF1; 2383; 7825127897b82ad6c4d944f6518f69b0\n" +
                "      NEWRECS.DB; 1272; be25eaeaa5bf6265e4ae7bee7ce582e7\n" +
                "      NEWS.WPG; 1130; 2a068de32adb6d656e1587d9c4564223\n" +
                "      NEWSFLSH.CMX; 15694; 8370f08d573ced7c782afd7dadbe32bc\n" +
                "      Newsletters.doc; 225792; c9f580db58f567d9757293e2ba307002\n" +
                "      Newt_Gingrich.vcf; 17589; 44810a4dd532ddb285d049034684dd9c\n" +
                "      OBJS3.PPT; 45455; 6a8309eb1511f2c0960dd28a0f9f55da\n" +
                "      pinata.png; 39193; 59c2a1e0a58659ada45bfb34e1f47af6\n" +
                "      PO.cdr; 45836; faa2e0c2853653a849af85d8772c5a6c\n" +
                "      POINT12.PPT; 53248; 42ecc8d72b67aece04f9c2318cc8e14b\n" +
                "      Presentation1.ppt; 53760; b4ccc9b6ce8f16b53c5537334004cb45\n" +
                "      WATERFAL.PCX; 39324; b754692f45816c25dfe3a2977ffd35e5\n" +
                "    disk.pbm; 521; bae5109911821269552cda40af7f756b\n" +
                "    goldstar.sdw; 234; b689819e2feddb4e927afaaa1d1b4605\n" +
                "    Gradient.wpg; 398; 790435da34d27e0727e444ef734a84e1\n" +
                "    INVDB.wk1; 486; 830d7dcce5a2ca2c911507d76dc9c39e\n" +
                "    INVDB.wks; 486; 9dbc7976b1f5a5df1f77dfd77dd4f5ef\n" +
                "    MINCOL2.RBF; 502; 4ec284022ac9b646d6f4e83dbc2d5503\n" +
                "    NOZZLE.DWG; 31906; 345b37583981bf06781cbbea33e4f206\n" +
                "    NOZZLE.DXF; 61832; 41a41f6eecb4e8d84750ebd593f61087\n" +
                "    OWTUTOR.WP; 32435; 380fb42da4339117a97ac66ff27e76d6\n" +
                "    PCT.ZIP; 84523; a9bcb32023f84dacf77127c97404b00b\n" +
                "    PRINT1.OBD; 100352; e5198546b2d5b385ae9ee4df0b5aa489\n" +
                "    ProfessionalReport.doc; 35840; 16ae7c300b4477b1aa1a483fa4ccc8f6\n" +
                "    sample.pst; 65536; 00592a3823aa91f1bcfaa8e8cf569234\n" +
                "    SELECT.CUR; 326; e23950223a5ff9ec4102eb5d28358187\n" +
                "    Shadow-Backcolour.vsd; 33280; c62f00774032cc3efc432ff46ac84202\n" +
                "    SYSTIMES.DBF; 427; 82fef39b1d00d57f0a65a09e1d3e7f14\n" +
                "    TEST1.DX2; 466; 132460d4ab8ed8ff8629554fe34c9de7\n" +
                "    text.txt; 50; c601184e96bafb99108ec20a1f5f23f0\n" +
                "    titleandchart.ppt; 37376; 238a47e33eb14694bce9b74cf829aecf\n" +
                "    tmp.pbm; 67533; 0b43d0815d0238f21ae99df0a6decb22\n" +
                "    WINSOCK.LZH; 85880; 15ad4473dfa891c0035d3c4749bca7cd\n" +
                "    WPFFORM4; 559; 529c04c8d15cf9e7421b3d80e132645a\n" +
                "    YESNO.MDB; 98304; e8de0c9ca68172048b56194d24f2845b\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testLinks() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ntfs/ntfs-links.dd"), "r");
        NTFSFileSystemType type = fss.getFileSystemType(NTFSFileSystemType.ID);
        NTFSFileSystem fs = type.create(device, true);

        String expectedStructure =
            "type: NTFS vol:links total:20971520 free:14782464\n" +
            "  .; \n" +
            "    $AttrDef; 2560; ad617ac3906958de35eacc3d90d31043\n" +
            "    $BadClus; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    $BadClus:$Bad; 20967424; 3880409d3ae930481b68e2c385e940d6\n" +
            "    $Bitmap; 640; 18ff823cc06f5467c31c43fdfedf6052\n" +
            "    $Boot; 8192; d1295f2004783b27bc5c4f22885146a4\n" +
            "    $Extend; \n" +
            "      $ObjId; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "      $Quota; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "      $Reparse; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "      $RmMetadata; \n" +
            "        $Repair; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "        $Repair:$Config; 8; 39d997ae6b77dab1dfbabb6cf0e4783a\n" +
            "        $Txf; \n" +
            "        $TxfLog; \n" +
            "          $Tops; 100; 4df1eb9ffd238625c60b930ef23ddf53\n" +
            "          $Tops:$T; 1048576; b6d81b360a5672d80c27430f39153e2c\n" +
            "          $TxfLog.blf; 65536; 1f070e71144dee6b3d873c034ff2defb\n" +
            "          $TxfLogContainer00000000000000000001; 1048576; c40dd0ff838002801b52df410e3b21fd\n" +
            "          $TxfLogContainer00000000000000000002; 1048576; b6d81b360a5672d80c27430f39153e2c\n" +
            "    $LogFile; 2097152; e5e93f63a0a66996e9ed79cdf110978d\n" +
            "    $MFT; 262144; e1c9dc6490312dda6b7f30d537e6d625\n" +
            "    $MFTMirr; 4096; 5042a0445a0e41ae9ff5353d80ea5e36\n" +
            "    $Secure; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    $Secure:$SDS; 263068; 8f916bba912579e3cc0f2d5a5be6e8a2\n" +
            "    $UpCase; 131072; 7ff498a44e45e77374cc7c962b1b92f2\n" +
            "    $Volume; 0; d41d8cd98f00b204e9800998ecf8427e\n" +
            "    dota2; \n" +
            "      Dota_2_Gameplay_Sep_2013.jpg; 21770; 918f521e9923cc77952bbc460b4fb864\n" +
            "      Dota_2_Gameplay_Sep_2013.jpg:Zone.Identifier; 26; fbccf14d504b7b2dbcb5a5bda75bd93b\n" +
            "      valve; \n" +
            "    half-life; \n" +
            "      Gabe Newell.jpg; 90404; 49a76a9957889cc8b0fed8753aac04ec\n" +
            "      Gabe Newell.jpg:Zone.Identifier; 26; fbccf14d504b7b2dbcb5a5bda75bd93b\n" +
            "      Half-Life_Cover_Art.jpg; 56606; d7417d54a527669278499e118caac66f\n" +
            "      Half-Life_Cover_Art.jpg:Zone.Identifier; 26; fbccf14d504b7b2dbcb5a5bda75bd93b\n" +
            "    valve; \n" +
            "      gaben.jpg; 90404; 49a76a9957889cc8b0fed8753aac04ec\n" +
            "      gaben.jpg:Zone.Identifier; 26; fbccf14d504b7b2dbcb5a5bda75bd93b\n";

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testFileSlackSpace() throws Exception {

        // Arrange
        device = new FileDevice(FileSystemTestUtils.getTestFile("test/fs/ntfs/compressed.dd"), "r");
        NTFSFileSystemType type = fss.getFileSystemType(NTFSFileSystemType.ID);
        NTFSFileSystem fs = type.create(device, true);

        // Act
        FileRecord fileRecord = fs.getNTFSVolume().getMFT().getRecordUnchecked(144); // XL3.XLS
        NTFSEntry entry = new NTFSEntry(fs, fileRecord, -1);
        FSFileSlackSpace fileSlackSpace = (FSFileSlackSpace) entry.getFile();
        byte[] slackSpace = fileSlackSpace.getSlackSpace();
        String md5 = DataStructureAsserts.getMD5Digest(slackSpace);

        // Assert
        Assert.assertEquals("Wrong length", 650, slackSpace.length);
        Assert.assertEquals("Wrong MD5", "5f7aec79cc32e8a3a64732e4652b3e32", md5);
    }
}

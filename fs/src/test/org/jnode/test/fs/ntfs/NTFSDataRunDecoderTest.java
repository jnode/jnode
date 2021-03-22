package org.jnode.test.fs.ntfs;

import java.util.List;
import org.jnode.fs.ntfs.NTFSStructure;
import org.jnode.fs.ntfs.datarun.DataRunDecoder;
import org.jnode.fs.ntfs.datarun.DataRunInterface;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.jnode.test.fs.FileSystemTestUtils.toByteArray;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link org.jnode.fs.ntfs.datarun.DataRunDecoder}.
 */
public class NTFSDataRunDecoderTest {

    @Test
    public void testSingleRunDecoding() {
        // Arrange
        byte[] buffer = toByteArray("21 40 AA 06 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(false, 1);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        assertThat(dataRunDecoder.getNumberOfVCNs(), is(64));

        String expectedRuns =
            "[data-run vcn:0-63 cluster:1706]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testMultiRunDecoding() {
        // Arrange
        byte[] buffer = toByteArray("21 04 B0 00 21 04 93 20 21 04 37 EF 21 08 77 2A 21 04 EC 08 21 04 19 04 21 04 62 01 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(false, 1);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        assertThat(dataRunDecoder.getNumberOfVCNs(), is(32));

        String expectedRuns =
            "[data-run vcn:0-3 cluster:176]\n" +
            "[data-run vcn:4-7 cluster:8515]\n" +
            "[data-run vcn:8-11 cluster:4218]\n" +
            "[data-run vcn:12-19 cluster:15089]\n" +
            "[data-run vcn:20-23 cluster:17373]\n" +
            "[data-run vcn:24-27 cluster:18422]\n" +
            "[data-run vcn:28-31 cluster:18776]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testCompressedRuns() {
        // Arrange
        byte[] buffer = toByteArray("21 07 B4 08 01 09 11 07 10 01 09 11 07 10 01 09 11 04 10 01 0C 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(true, 16);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        assertThat(dataRunDecoder.getNumberOfVCNs(), is(64));

        String expectedRuns =
            "[compressed-run vcn:0-15 [[data-run vcn:0-6 cluster:2228]]]\n" +
            "[compressed-run vcn:16-31 [[data-run vcn:16-22 cluster:2244]]]\n" +
            "[compressed-run vcn:32-47 [[data-run vcn:32-38 cluster:2260]]]\n" +
            "[compressed-run vcn:48-63 [[data-run vcn:48-51 cluster:2276]]]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testLargeSetOfRuns() {
        // Arrange
        byte[] buffer = toByteArray("31 06 29 C3 02 01 0A 11 06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A 11 " +
            "06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A " +
            "11 06 06 01 0A 11 06 06 01 0A 11 06 06 01 0A 31 06 69 E6 6A 01 0A 21 06 54 14 01 0A 21 06 6D 0B 01 0A " +
            "31 06 87 71 0B 01 0A 31 06 1C AB E2 01 0A 31 06 88 0D F5 01 0A 31 02 EC 2B DE 01 0E 32 C1 00 71 94 3E " +
            "01 0F 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(true, 16);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        assertThat(dataRunDecoder.getNumberOfVCNs(), is(544));

        String expectedRuns =
            "[compressed-run vcn:0-15 [[data-run vcn:0-5 cluster:181033]]]\n" +
            "[compressed-run vcn:16-31 [[data-run vcn:16-21 cluster:181039]]]\n" +
            "[compressed-run vcn:32-47 [[data-run vcn:32-37 cluster:181045]]]\n" +
            "[compressed-run vcn:48-63 [[data-run vcn:48-53 cluster:181051]]]\n" +
            "[compressed-run vcn:64-79 [[data-run vcn:64-69 cluster:181057]]]\n" +
            "[compressed-run vcn:80-95 [[data-run vcn:80-85 cluster:181063]]]\n" +
            "[compressed-run vcn:96-111 [[data-run vcn:96-101 cluster:181069]]]\n" +
            "[compressed-run vcn:112-127 [[data-run vcn:112-117 cluster:181075]]]\n" +
            "[compressed-run vcn:128-143 [[data-run vcn:128-133 cluster:181081]]]\n" +
            "[compressed-run vcn:144-159 [[data-run vcn:144-149 cluster:181087]]]\n" +
            "[compressed-run vcn:160-175 [[data-run vcn:160-165 cluster:181093]]]\n" +
            "[compressed-run vcn:176-191 [[data-run vcn:176-181 cluster:181099]]]\n" +
            "[compressed-run vcn:192-207 [[data-run vcn:192-197 cluster:181105]]]\n" +
            "[compressed-run vcn:208-223 [[data-run vcn:208-213 cluster:181111]]]\n" +
            "[compressed-run vcn:224-239 [[data-run vcn:224-229 cluster:7186912]]]\n" +
            "[compressed-run vcn:240-255 [[data-run vcn:240-245 cluster:7192116]]]\n" +
            "[compressed-run vcn:256-271 [[data-run vcn:256-261 cluster:7195041]]]\n" +
            "[compressed-run vcn:272-287 [[data-run vcn:272-277 cluster:7945000]]]\n" +
            "[compressed-run vcn:288-303 [[data-run vcn:288-293 cluster:6022724]]]\n" +
            "[compressed-run vcn:304-319 [[data-run vcn:304-309 cluster:5305292]]]\n" +
            "[compressed-run vcn:320-335 [[data-run vcn:320-321 cluster:3088312]]]\n" +
            "[data-run vcn:336-527 cluster:7189545]\n" +
            "[compressed-run vcn:528-543 [[data-run vcn:528-528 cluster:7189737]]]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testCompressedRuns_WithoutSparseRuns() {
        // Arrange
        byte[] buffer = toByteArray(
            "31 03 EE 0A 01 11 01 F5\n" +
                "01 0C 21 04 8A CB 01 0C 21 04 D5 0F 01 0C 21 04\n" +
                "F7 AB 01 0C 21 04 80 17 01 0C 21 04 A2 13 01 0C\n" +
                "21 04 6D 01 01 0C 21 04 AA 14 01 0C 21 04 E4 02\n" +
                "01 0C 21 04 05 03 01 0C 21 03 7B 0A 21 01 75 F7\n" +
                "01 0C 21 03 66 03 21 01 D4 F9 01 0C 31 04 B0 40\n" +
                "FF 01 00 00 00 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(true, 16);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        String expectedRuns =
            "[compressed-run vcn:0-15 [[data-run vcn:0-2 cluster:68334], [data-run vcn:3-3 cluster:68323]]]\n" +
            "[compressed-run vcn:16-31 [[data-run vcn:16-19 cluster:54893]]]\n" +
            "[compressed-run vcn:32-47 [[data-run vcn:32-35 cluster:58946]]]\n" +
            "[compressed-run vcn:48-63 [[data-run vcn:48-51 cluster:37433]]]\n" +
            "[compressed-run vcn:64-79 [[data-run vcn:64-67 cluster:43449]]]\n" +
            "[compressed-run vcn:80-95 [[data-run vcn:80-83 cluster:48475]]]\n" +
            "[compressed-run vcn:96-111 [[data-run vcn:96-99 cluster:48840]]]\n" +
            "[compressed-run vcn:112-127 [[data-run vcn:112-115 cluster:54130]]]\n" +
            "[compressed-run vcn:128-143 [[data-run vcn:128-131 cluster:54870]]]\n" +
            "[compressed-run vcn:144-159 [[data-run vcn:144-147 cluster:55643]]]\n" +
            "[compressed-run vcn:160-175 [[data-run vcn:160-162 cluster:58326], [data-run vcn:163-163 cluster:56139]]]\n" +
            "[compressed-run vcn:176-191 [[data-run vcn:176-178 cluster:57009], [data-run vcn:179-179 cluster:55429]]]\n" +
            "[compressed-run vcn:192-207 [[data-run vcn:192-195 cluster:6453]]]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testCombiningSubsequentAttributesRuns() {
        // Unfortunately this is a massive test case; the trivial cases don't exhibit this merging requirement

        // Arrange
        byte[] buffer1 = toByteArray(
            "31 04 65 19 01 01 0C 21 04 E9 09 01 0C 21 04 21 16 01 0C 21 04 11 20 01 0C 21 04 DC 0D 01 0C 21 " +
            "04 5A 0C 01 0C 21 04 08 17 01 0C 31 04 ED 8B FE 01 0C 21 04 B3 1B 01 0C 21 03 44 15 21 01 19 08 " +
            "01 0C 31 04 D7 87 00 01 0C 21 04 20 1A 01 0C 21 04 21 58 01 0C 21 04 93 2E 01 0C 21 04 75 BD 01 " +
            "0C 21 01 9B 06 11 01 AB 21 01 BC FD 21 01 A8 D4 01 0C 21 04 8D 95 01 0C 21 04 1E 0B 01 0C 31 04 " +
            "9F C1 00 01 0C 21 04 64 D2 01 0C 21 03 65 C3 21 01 03 FE 01 0C 21 04 83 B8 01 0C 21 04 1F F6 01 " +
            "0C 31 04 CF 6F FF 01 0C 31 04 E8 BB 00 01 0C 31 04 DE 65 FF 01 0C 31 04 A4 F5 00 01 0C 31 04 32 " +
            "5E FF 01 0C 31 04 73 A0 00 01 0C 31 04 48 26 FF 01 0C 21 04 C6 FB 01 0C 21 04 E5 3B 01 0C 31 04 " +
            "0E 90 00 01 0C 31 04 BF F6 FE 01 0C 31 04 DA CE 00 01 0C 21 04 E2 86 01 0C 31 04 BB 83 00 01 0C " +
            "31 04 2A 49 FF 01 0C 31 03 7F D5 00 11 01 1E 01 0C 31 04 B5 13 FF 01 0C 31 03 D8 FA 00 21 01 15 " +
            "FF 01 0C 31 04 0A 3A FF 01 0C 21 04 A7 AD 01 0C 31 04 D0 5D 01 01 0C 21 03 47 DD 21 01 B8 FD 01 " +
            "0C 31 04 9E 74 FF 01 0C 21 04 28 9D 01 0C 21 04 3B 4F 01 0C 31 04 08 B2 00 01 0C 31 04 8D 63 FF " +
            "01 0C 21 04 80 0D 01 0C 21 04 49 7C 01 0C 21 04 E8 E4 01 0C 21 03 FC 39 21 01 3B 01 01 0C 31 04 " +
            "6F AB FE 01 0C 31 04 70 CA 00 01 0C 21 04 5C 7C 01 0C 21 04 82 2E 01 0C 31 04 32 C5 FE 01 0C 31 " +
            "02 11 CC 00 11 02 F6 01 0C 31 03 7E 38 FF 11 01 16 01 0C 21 04 96 0E 00");

        byte[] buffer2 = toByteArray(
            "01 0C 31 04 B0 0D 01 01 0C 21 04 BF 72 01 0C 21 02 61 EE 21 02 7C FF 01 0C 31 04 0F ED FE 01 0C " +
            "21 04 58 43 01 0C 21 04 48 66 01 0C 31 04 E2 84 00 01 0C 21 04 F3 DE 01 0C 21 03 78 92 21 01 F5 " +
            "FC 01 0C 21 04 6F B4 01 0C 21 04 85 7E 01 0C 21 04 3E 24 01 0C 31 04 F5 11 FF 01 0C 31 04 3F 02 " +
            "01 01 0C 31 04 76 D3 FE 01 0C 31 04 C5 E6 00 01 0C 21 04 4C 51 01 0C 31 04 BB A9 FE 01 0C 31 02 " +
            "74 EE 00 11 02 F6 01 0C 21 02 81 B6 11 02 B8 01 0C 31 04 E5 5F FF 01 0C 31 04 FA D3 00 01 0C 21 " +
            "04 9E 34 01 0C 21 04 CC 62 01 0C 31 04 52 0A FF 01 0C 11 04 B6 01 0C 31 04 81 97 00 01 0C 21 04 " +
            "9F 80 01 0C 21 04 80 50 01 0C 31 04 37 75 FF 01 0C 31 02 73 9E 00 21 02 51 FD 01 0C 31 04 0D 20 " +
            "FF 01 0C 21 04 B7 28 01 0C 21 04 C2 65 01 0C 21 04 19 1C 01 0C 21 04 4C 24 01 0C 21 04 3F 3C 01 " +
            "0C 21 04 32 1A 01 0C 21 04 EB 32 01 0C 31 04 DB A0 FE 01 0C 31 04 B0 52 01 01 0C 21 03 FF 14 11 " +
            "01 F9 01 0C 21 03 7F D9 21 01 97 FE 01 0C 31 03 6C EE FE 11 01 29 01 0C 11 04 01 01 0C 21 04 25 " +
            "6B 01 0C 21 04 25 F7 01 0C 21 04 71 48 01 0C 21 04 EC 74 01 0C 31 03 3A CD FE 11 01 14 01 0C 21 " +
            "04 02 DD 01 0C 21 04 A6 4E 01 0C 21 04 19 75 01 0C 21 04 A4 00 01 0C 21 04 D2 37 01 0C 21 04 DD " +
            "11 01 0C 21 04 87 3C 01 0C 21 04 39 17 01 0C 31 04 84 6A FF 01 0C 21 04 20 CF 01 0C 31 04 DE CC " +
            "00 01 0C 31 01 A9 BB FE 11 01 DC 21 01 62 FD 21 01 5E FD 01 0C 21 04 10 E6 01 0C 21 04 37 18 01 " +
            "0C 21 04 D2 22 01 0C 21 04 A6 12 01 0C 21 04 5E 2E 01 0C 21 04 CB 1D 01 0C 21 04 F4 45 01 0C 21 " +
            "04 97 00 01 0C 21 04 8D 19 01 0C 21 04 94 28 01 0C 21 04 38 17 01 0C 21 04 B3 1C 01 0C 31 04 CD " +
            "96 FE 01 0C 31 04 E3 99 00 01 0C 21 04 FF 81 01 0C 21 04 B9 62 01 0C 21 04 08 01 01 0C 21 04 91 " +
            "80 01 0C 21 04 9D 12 01 0C 21 04 0E 25 01 0C 21 04 DA 1E 01 0C 21 04 12 2A 01 0C 21 04 EE 0C 01 " +
            "0C 21 04 55 36 01 0C 21 04 9C 1F 01 0C 21 04 47 24 01 0C 21 04 FD 14 01 0C 21 04 05 A2 01 0C 31 " +
            "04 72 63 FF 01 0C 31 04 54 E5 00 01 0C 21 04 45 E5 01 0C 31 03 6C 88 00 11 01 51 01 0C 21 04 95 " +
            "E2 01 0C 21 04 F5 13 01 0C 21 03 CF AE 21 01 F8 FC 01 0C 21 03 40 F1 21 01 78 FA 01 0C 21 04 86 " +
            "9C 01 0C 21 04 41 02 01 0C 21 04 95 2F 01 0C 21 03 8A 0E 11 01 1B 01 0C 21 03 10 BD 21 01 07 FA " +
            "01 0C 31 04 6F 79 FF 01 0C 21 04 30 E4 01 0C 21 04 9E 30 01 0C 21 04 E7 00 01 0C 21 04 49 2B 01 " +
            "0C 21 04 6E 01 01 0C 21 04 2B 12 01 0C 21 04 C2 21 01 0C 21 04 6F 19 01 0C 21 04 4A 09 01 0C 21 " +
            "04 4D 18 01 0C 11 04 29 01 0C 21 04 C5 25 01 0C 21 04 6D 2C 01 0C 11 04 22 01 0C 21 04 C5 00 01 " +
            "0C 21 04 62 07 01 0C 21 04 FA 2C 01 0C 21 04 97 16 01 0C 21 04 C7 15 01 0C 31 04 3D D2 FE 01 0C " +
            "31 04 D9 30 01 01 0C 31 04 C7 7B FE 01 0C 21 04 58 73 01 0C 31 04 E7 90 00 01 0C 21 04 92 01 01 " +
            "0C 21 04 D8 14 01 0C 21 04 2B 01 01 0C 21 04 83 F1 01 0C 00 ");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(true, 16);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer1, 0), 0);
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer2, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        String expectedRuns =
            "[compressed-run vcn:0-15 [[data-run vcn:0-3 cluster:72037]]]\n" +
            "[compressed-run vcn:16-31 [[data-run vcn:16-19 cluster:74574]]]\n" +
            "[compressed-run vcn:32-47 [[data-run vcn:32-35 cluster:80239]]]\n" +
            "[compressed-run vcn:48-63 [[data-run vcn:48-51 cluster:88448]]]\n" +
            "[compressed-run vcn:64-79 [[data-run vcn:64-67 cluster:91996]]]\n" +
            "[compressed-run vcn:80-95 [[data-run vcn:80-83 cluster:95158]]]\n" +
            "[compressed-run vcn:96-111 [[data-run vcn:96-99 cluster:101054]]]\n" +
            "[compressed-run vcn:112-127 [[data-run vcn:112-115 cluster:5803]]]\n" +
            "[compressed-run vcn:128-143 [[data-run vcn:128-131 cluster:12894]]]\n" +
            "[compressed-run vcn:144-159 [[data-run vcn:144-146 cluster:18338], [data-run vcn:147-147 cluster:20411]]]\n" +
            "[compressed-run vcn:160-175 [[data-run vcn:160-163 cluster:55186]]]\n" +
            "[compressed-run vcn:176-191 [[data-run vcn:176-179 cluster:61874]]]\n" +
            "[compressed-run vcn:192-207 [[data-run vcn:192-195 cluster:84435]]]\n" +
            "[compressed-run vcn:208-223 [[data-run vcn:208-211 cluster:96358]]]\n" +
            "[compressed-run vcn:224-239 [[data-run vcn:224-227 cluster:79323]]]\n" +
            "[compressed-run vcn:240-255 [[data-run vcn:240-240 cluster:81014], [data-run vcn:241-241 cluster:80929], [data-run vcn:242-242 cluster:80349], [data-run vcn:243-243 cluster:69253]]]\n" +
            "[compressed-run vcn:256-271 [[data-run vcn:256-259 cluster:42002]]]\n" +
            "[compressed-run vcn:272-287 [[data-run vcn:272-275 cluster:44848]]]\n" +
            "[compressed-run vcn:288-303 [[data-run vcn:288-291 cluster:94415]]]\n" +
            "[compressed-run vcn:304-319 [[data-run vcn:304-307 cluster:82739]]]\n" +
            "[compressed-run vcn:320-335 [[data-run vcn:320-322 cluster:67224], [data-run vcn:323-323 cluster:66715]]]\n" +
            "[compressed-run vcn:336-351 [[data-run vcn:336-339 cluster:48414]]]\n" +
            "[compressed-run vcn:352-367 [[data-run vcn:352-355 cluster:45885]]]\n" +
            "[compressed-run vcn:368-383 [[data-run vcn:368-371 cluster:8972]]]\n" +
            "[compressed-run vcn:384-399 [[data-run vcn:384-387 cluster:57076]]]\n" +
            "[compressed-run vcn:400-415 [[data-run vcn:400-403 cluster:17618]]]\n" +
            "[compressed-run vcn:416-431 [[data-run vcn:416-419 cluster:80502]]]\n" +
            "[compressed-run vcn:432-447 [[data-run vcn:432-435 cluster:39080]]]\n" +
            "[compressed-run vcn:448-463 [[data-run vcn:448-451 cluster:80155]]]\n" +
            "[compressed-run vcn:464-479 [[data-run vcn:464-467 cluster:24419]]]\n" +
            "[compressed-run vcn:480-495 [[data-run vcn:480-483 cluster:23337]]]\n" +
            "[compressed-run vcn:496-511 [[data-run vcn:496-499 cluster:38670]]]\n" +
            "[compressed-run vcn:512-527 [[data-run vcn:512-515 cluster:75548]]]\n" +
            "[compressed-run vcn:528-543 [[data-run vcn:528-531 cluster:7643]]]\n" +
            "[compressed-run vcn:544-559 [[data-run vcn:544-547 cluster:60597]]]\n" +
            "[compressed-run vcn:560-575 [[data-run vcn:560-563 cluster:29591]]]\n" +
            "[compressed-run vcn:576-591 [[data-run vcn:576-579 cluster:63314]]]\n" +
            "[compressed-run vcn:592-607 [[data-run vcn:592-595 cluster:16508]]]\n" +
            "[compressed-run vcn:608-623 [[data-run vcn:608-610 cluster:71163], [data-run vcn:611-611 cluster:71193]]]\n" +
            "[compressed-run vcn:624-639 [[data-run vcn:624-627 cluster:10702]]]\n" +
            "[compressed-run vcn:640-655 [[data-run vcn:640-642 cluster:74918], [data-run vcn:643-643 cluster:74683]]]\n" +
            "[compressed-run vcn:656-671 [[data-run vcn:656-659 cluster:24005]]]\n" +
            "[compressed-run vcn:672-687 [[data-run vcn:672-675 cluster:2924]]]\n" +
            "[compressed-run vcn:688-703 [[data-run vcn:688-691 cluster:92476]]]\n" +
            "[compressed-run vcn:704-719 [[data-run vcn:704-706 cluster:83587], [data-run vcn:707-707 cluster:83003]]]\n" +
            "[compressed-run vcn:720-735 [[data-run vcn:720-723 cluster:47321]]]\n" +
            "[compressed-run vcn:736-751 [[data-run vcn:736-739 cluster:22017]]]\n" +
            "[compressed-run vcn:752-767 [[data-run vcn:752-755 cluster:42300]]]\n" +
            "[compressed-run vcn:768-783 [[data-run vcn:768-771 cluster:87876]]]\n" +
            "[compressed-run vcn:784-799 [[data-run vcn:784-787 cluster:47825]]]\n" +
            "[compressed-run vcn:800-815 [[data-run vcn:800-803 cluster:51281]]]\n" +
            "[compressed-run vcn:816-831 [[data-run vcn:816-819 cluster:83098]]]\n" +
            "[compressed-run vcn:832-847 [[data-run vcn:832-835 cluster:76162]]]\n" +
            "[compressed-run vcn:848-863 [[data-run vcn:848-850 cluster:91006], [data-run vcn:851-851 cluster:91321]]]\n" +
            "[compressed-run vcn:864-879 [[data-run vcn:864-867 cluster:4136]]]\n" +
            "[compressed-run vcn:880-895 [[data-run vcn:880-883 cluster:55960]]]\n" +
            "[compressed-run vcn:896-911 [[data-run vcn:896-899 cluster:87796]]]\n" +
            "[compressed-run vcn:912-927 [[data-run vcn:912-915 cluster:99702]]]\n" +
            "[compressed-run vcn:928-943 [[data-run vcn:928-931 cluster:19112]]]\n" +
            "[compressed-run vcn:944-959 [[data-run vcn:944-945 cluster:71353], [data-run vcn:946-947 cluster:71343]]]\n" +
            "[compressed-run vcn:960-975 [[data-run vcn:960-962 cluster:20269], [data-run vcn:963-963 cluster:20291]]]\n" +
            "[compressed-run vcn:976-991 [[data-run vcn:976-979 cluster:24025]]]\n" +
            "[compressed-run vcn:992-1007 [[data-run vcn:992-995 cluster:69040]]]\n" +
            "[compressed-run vcn:1008-1023 [[data-run vcn:1008-1011 cluster:98415]]]\n" +
            "[compressed-run vcn:1024-1039 [[data-run vcn:1024-1025 cluster:93904], [data-run vcn:1026-1027 cluster:93772]]]\n" +
            "[compressed-run vcn:1040-1055 [[data-run vcn:1040-1043 cluster:23387]]]\n" +
            "[compressed-run vcn:1056-1071 [[data-run vcn:1056-1059 cluster:40627]]]\n" +
            "[compressed-run vcn:1072-1087 [[data-run vcn:1072-1075 cluster:66811]]]\n" +
            "[compressed-run vcn:1088-1103 [[data-run vcn:1088-1091 cluster:100829]]]\n" +
            "[compressed-run vcn:1104-1119 [[data-run vcn:1104-1107 cluster:92368]]]\n" +
            "[compressed-run vcn:1120-1135 [[data-run vcn:1120-1122 cluster:64328], [data-run vcn:1123-1123 cluster:63549]]]\n" +
            "[compressed-run vcn:1136-1151 [[data-run vcn:1136-1139 cluster:44204]]]\n" +
            "[compressed-run vcn:1152-1167 [[data-run vcn:1152-1155 cluster:76593]]]\n" +
            "[compressed-run vcn:1168-1183 [[data-run vcn:1168-1171 cluster:85871]]]\n" +
            "[compressed-run vcn:1184-1199 [[data-run vcn:1184-1187 cluster:24932]]]\n" +
            "[compressed-run vcn:1200-1215 [[data-run vcn:1200-1203 cluster:91043]]]\n" +
            "[compressed-run vcn:1216-1231 [[data-run vcn:1216-1219 cluster:14105]]]\n" +
            "[compressed-run vcn:1232-1247 [[data-run vcn:1232-1235 cluster:73182]]]\n" +
            "[compressed-run vcn:1248-1263 [[data-run vcn:1248-1251 cluster:93994]]]\n" +
            "[compressed-run vcn:1264-1279 [[data-run vcn:1264-1267 cluster:6373]]]\n" +
            "[compressed-run vcn:1280-1295 [[data-run vcn:1280-1281 cluster:67417], [data-run vcn:1282-1283 cluster:67407]]]\n" +
            "[compressed-run vcn:1296-1311 [[data-run vcn:1296-1297 cluster:48592], [data-run vcn:1298-1299 cluster:48520]]]\n" +
            "[compressed-run vcn:1312-1327 [[data-run vcn:1312-1315 cluster:7533]]]\n" +
            "[compressed-run vcn:1328-1343 [[data-run vcn:1328-1331 cluster:61799]]]\n" +
            "[compressed-run vcn:1344-1359 [[data-run vcn:1344-1347 cluster:75269]]]\n" +
            "[compressed-run vcn:1360-1375 [[data-run vcn:1360-1363 cluster:100561]]]\n" +
            "[compressed-run vcn:1376-1391 [[data-run vcn:1376-1379 cluster:37667]]]\n" +
            "[compressed-run vcn:1392-1407 [[data-run vcn:1392-1395 cluster:37593]]]\n" +
            "[compressed-run vcn:1408-1423 [[data-run vcn:1408-1411 cluster:76378]]]\n" +
            "[compressed-run vcn:1424-1439 [[data-run vcn:1424-1427 cluster:43769]]]\n" +
            "[compressed-run vcn:1440-1455 [[data-run vcn:1440-1443 cluster:64377]]]\n" +
            "[compressed-run vcn:1456-1471 [[data-run vcn:1456-1459 cluster:28848]]]\n" +
            "[compressed-run vcn:1472-1487 [[data-run vcn:1472-1473 cluster:69411], [data-run vcn:1474-1475 cluster:68724]]]\n" +
            "[compressed-run vcn:1488-1503 [[data-run vcn:1488-1491 cluster:11393]]]\n" +
            "[compressed-run vcn:1504-1519 [[data-run vcn:1504-1507 cluster:21816]]]\n" +
            "[compressed-run vcn:1520-1535 [[data-run vcn:1520-1523 cluster:47866]]]\n" +
            "[compressed-run vcn:1536-1551 [[data-run vcn:1536-1539 cluster:55059]]]\n" +
            "[compressed-run vcn:1552-1567 [[data-run vcn:1552-1555 cluster:64351]]]\n" +
            "[compressed-run vcn:1568-1583 [[data-run vcn:1568-1571 cluster:79774]]]\n" +
            "[compressed-run vcn:1584-1599 [[data-run vcn:1584-1587 cluster:86480]]]\n" +
            "[compressed-run vcn:1600-1615 [[data-run vcn:1600-1603 cluster:99515]]]\n" +
            "[compressed-run vcn:1616-1631 [[data-run vcn:1616-1619 cluster:9622]]]\n" +
            "[compressed-run vcn:1632-1647 [[data-run vcn:1632-1635 cluster:96326]]]\n" +
            "[compressed-run vcn:1648-1663 [[data-run vcn:1648-1650 cluster:101701], [data-run vcn:1651-1651 cluster:101694]]]\n" +
            "[compressed-run vcn:1664-1679 [[data-run vcn:1664-1666 cluster:91837], [data-run vcn:1667-1667 cluster:91476]]]\n" +
            "[compressed-run vcn:1680-1695 [[data-run vcn:1680-1682 cluster:21440], [data-run vcn:1683-1683 cluster:21481]]]\n" +
            "[compressed-run vcn:1696-1711 [[data-run vcn:1696-1699 cluster:21482]]]\n" +
            "[compressed-run vcn:1712-1727 [[data-run vcn:1712-1715 cluster:48911]]]\n" +
            "[compressed-run vcn:1728-1743 [[data-run vcn:1728-1731 cluster:46644]]]\n" +
            "[compressed-run vcn:1744-1759 [[data-run vcn:1744-1747 cluster:65189]]]\n" +
            "[compressed-run vcn:1760-1775 [[data-run vcn:1760-1763 cluster:95121]]]\n" +
            "[compressed-run vcn:1776-1791 [[data-run vcn:1776-1778 cluster:16587], [data-run vcn:1779-1779 cluster:16607]]]\n" +
            "[compressed-run vcn:1792-1807 [[data-run vcn:1792-1795 cluster:7649]]]\n" +
            "[compressed-run vcn:1808-1823 [[data-run vcn:1808-1811 cluster:27783]]]\n" +
            "[compressed-run vcn:1824-1839 [[data-run vcn:1824-1827 cluster:57760]]]\n" +
            "[compressed-run vcn:1840-1855 [[data-run vcn:1840-1843 cluster:57924]]]\n" +
            "[compressed-run vcn:1856-1871 [[data-run vcn:1856-1859 cluster:72214]]]\n" +
            "[compressed-run vcn:1872-1887 [[data-run vcn:1872-1875 cluster:76787]]]\n" +
            "[compressed-run vcn:1888-1903 [[data-run vcn:1888-1891 cluster:92282]]]\n" +
            "[compressed-run vcn:1904-1919 [[data-run vcn:1904-1907 cluster:98227]]]\n" +
            "[compressed-run vcn:1920-1935 [[data-run vcn:1920-1923 cluster:59959]]]\n" +
            "[compressed-run vcn:1936-1951 [[data-run vcn:1936-1939 cluster:47447]]]\n" +
            "[compressed-run vcn:1952-1967 [[data-run vcn:1952-1955 cluster:99893]]]\n" +
            "[compressed-run vcn:1968-1983 [[data-run vcn:1968-1968 cluster:16862], [data-run vcn:1969-1969 cluster:16826], [data-run vcn:1970-1970 cluster:16156], [data-run vcn:1971-1971 cluster:15482]]]\n" +
            "[compressed-run vcn:1984-1999 [[data-run vcn:1984-1987 cluster:8842]]]\n" +
            "[compressed-run vcn:2000-2015 [[data-run vcn:2000-2003 cluster:15041]]]\n" +
            "[compressed-run vcn:2016-2031 [[data-run vcn:2016-2019 cluster:23955]]]\n" +
            "[compressed-run vcn:2032-2047 [[data-run vcn:2032-2035 cluster:28729]]]\n" +
            "[compressed-run vcn:2048-2063 [[data-run vcn:2048-2051 cluster:40599]]]\n" +
            "[compressed-run vcn:2064-2079 [[data-run vcn:2064-2067 cluster:48226]]]\n" +
            "[compressed-run vcn:2080-2095 [[data-run vcn:2080-2083 cluster:66134]]]\n" +
            "[compressed-run vcn:2096-2111 [[data-run vcn:2096-2099 cluster:66285]]]\n" +
            "[compressed-run vcn:2112-2127 [[data-run vcn:2112-2115 cluster:72826]]]\n" +
            "[compressed-run vcn:2128-2143 [[data-run vcn:2128-2131 cluster:83214]]]\n" +
            "[compressed-run vcn:2144-2159 [[data-run vcn:2144-2147 cluster:89158]]]\n" +
            "[compressed-run vcn:2160-2175 [[data-run vcn:2160-2163 cluster:96505]]]\n" +
            "[compressed-run vcn:2176-2191 [[data-run vcn:2176-2179 cluster:4038]]]\n" +
            "[compressed-run vcn:2192-2207 [[data-run vcn:2192-2195 cluster:43433]]]\n" +
            "[compressed-run vcn:2208-2223 [[data-run vcn:2208-2211 cluster:11176]]]\n" +
            "[compressed-run vcn:2224-2239 [[data-run vcn:2224-2227 cluster:36449]]]\n" +
            "[compressed-run vcn:2240-2255 [[data-run vcn:2240-2243 cluster:36713]]]\n" +
            "[compressed-run vcn:2256-2271 [[data-run vcn:2256-2259 cluster:4090]]]\n" +
            "[compressed-run vcn:2272-2287 [[data-run vcn:2272-2275 cluster:8855]]]\n" +
            "[compressed-run vcn:2288-2303 [[data-run vcn:2288-2291 cluster:18341]]]\n" +
            "[compressed-run vcn:2304-2319 [[data-run vcn:2304-2307 cluster:26239]]]\n" +
            "[compressed-run vcn:2320-2335 [[data-run vcn:2320-2323 cluster:37009]]]\n" +
            "[compressed-run vcn:2336-2351 [[data-run vcn:2336-2339 cluster:40319]]]\n" +
            "[compressed-run vcn:2352-2367 [[data-run vcn:2352-2355 cluster:54228]]]\n" +
            "[compressed-run vcn:2368-2383 [[data-run vcn:2368-2371 cluster:62320]]]\n" +
            "[compressed-run vcn:2384-2399 [[data-run vcn:2384-2387 cluster:71607]]]\n" +
            "[compressed-run vcn:2400-2415 [[data-run vcn:2400-2403 cluster:76980]]]\n" +
            "[compressed-run vcn:2416-2431 [[data-run vcn:2416-2419 cluster:52921]]]\n" +
            "[compressed-run vcn:2432-2447 [[data-run vcn:2432-2435 cluster:12843]]]\n" +
            "[compressed-run vcn:2448-2463 [[data-run vcn:2448-2451 cluster:71551]]]\n" +
            "[compressed-run vcn:2464-2479 [[data-run vcn:2464-2467 cluster:64708]]]\n" +
            "[compressed-run vcn:2480-2495 [[data-run vcn:2480-2482 cluster:99632], [data-run vcn:2483-2483 cluster:99713]]]\n" +
            "[compressed-run vcn:2496-2511 [[data-run vcn:2496-2499 cluster:92182]]]\n" +
            "[compressed-run vcn:2512-2527 [[data-run vcn:2512-2515 cluster:97291]]]\n" +
            "[compressed-run vcn:2528-2543 [[data-run vcn:2528-2530 cluster:76506], [data-run vcn:2531-2531 cluster:75730]]]\n" +
            "[compressed-run vcn:2544-2559 [[data-run vcn:2544-2546 cluster:71954], [data-run vcn:2547-2547 cluster:70538]]]\n" +
            "[compressed-run vcn:2560-2575 [[data-run vcn:2560-2563 cluster:45072]]]\n" +
            "[compressed-run vcn:2576-2591 [[data-run vcn:2576-2579 cluster:45649]]]\n" +
            "[compressed-run vcn:2592-2607 [[data-run vcn:2592-2595 cluster:57830]]]\n" +
            "[compressed-run vcn:2608-2623 [[data-run vcn:2608-2610 cluster:61552], [data-run vcn:2611-2611 cluster:61579]]]\n" +
            "[compressed-run vcn:2624-2639 [[data-run vcn:2624-2626 cluster:44443], [data-run vcn:2627-2627 cluster:42914]]]\n" +
            "[compressed-run vcn:2640-2655 [[data-run vcn:2640-2643 cluster:8465]]]\n" +
            "[compressed-run vcn:2656-2671 [[data-run vcn:2656-2659 cluster:1345]]]\n" +
            "[compressed-run vcn:2672-2687 [[data-run vcn:2672-2675 cluster:13791]]]\n" +
            "[compressed-run vcn:2688-2703 [[data-run vcn:2688-2691 cluster:14022]]]\n" +
            "[compressed-run vcn:2704-2719 [[data-run vcn:2704-2707 cluster:25103]]]\n" +
            "[compressed-run vcn:2720-2735 [[data-run vcn:2720-2723 cluster:25469]]]\n" +
            "[compressed-run vcn:2736-2751 [[data-run vcn:2736-2739 cluster:30120]]]\n" +
            "[compressed-run vcn:2752-2767 [[data-run vcn:2752-2755 cluster:38762]]]\n" +
            "[compressed-run vcn:2768-2783 [[data-run vcn:2768-2771 cluster:45273]]]\n" +
            "[compressed-run vcn:2784-2799 [[data-run vcn:2784-2787 cluster:47651]]]\n" +
            "[compressed-run vcn:2800-2815 [[data-run vcn:2800-2803 cluster:53872]]]\n" +
            "[compressed-run vcn:2816-2831 [[data-run vcn:2816-2819 cluster:53913]]]\n" +
            "[compressed-run vcn:2832-2847 [[data-run vcn:2832-2835 cluster:63582]]]\n" +
            "[compressed-run vcn:2848-2863 [[data-run vcn:2848-2851 cluster:74955]]]\n" +
            "[compressed-run vcn:2864-2879 [[data-run vcn:2864-2867 cluster:74989]]]\n" +
            "[compressed-run vcn:2880-2895 [[data-run vcn:2880-2883 cluster:75186]]]\n" +
            "[compressed-run vcn:2896-2911 [[data-run vcn:2896-2899 cluster:77076]]]\n" +
            "[compressed-run vcn:2912-2927 [[data-run vcn:2912-2915 cluster:88590]]]\n" +
            "[compressed-run vcn:2928-2943 [[data-run vcn:2928-2931 cluster:94373]]]\n" +
            "[compressed-run vcn:2944-2959 [[data-run vcn:2944-2947 cluster:99948]]]\n" +
            "[compressed-run vcn:2960-2975 [[data-run vcn:2960-2963 cluster:22697]]]\n" +
            "[compressed-run vcn:2976-2991 [[data-run vcn:2976-2979 cluster:100738]]]\n" +
            "[compressed-run vcn:2992-3007 [[data-run vcn:2992-2995 cluster:1353]]]\n" +
            "[compressed-run vcn:3008-3023 [[data-run vcn:3008-3011 cluster:30881]]]\n" +
            "[compressed-run vcn:3024-3039 [[data-run vcn:3024-3027 cluster:67976]]]\n" +
            "[compressed-run vcn:3040-3055 [[data-run vcn:3040-3043 cluster:68378]]]\n" +
            "[compressed-run vcn:3056-3071 [[data-run vcn:3056-3059 cluster:73714]]]\n" +
            "[compressed-run vcn:3072-3087 [[data-run vcn:3072-3075 cluster:74013]]]\n" +
            "[compressed-run vcn:3088-3103 [[data-run vcn:3088-3091 cluster:70304]]]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testDataRunWithLargeNegativeOffset() {
        // Arrange
        byte[] buffer = toByteArray(
            "33 C0 3B 01 00 00 0C 43 14 C8 00 2C 43 F5 1E 43 F1 15 01 63 63 EB 25 42 A7 77 FA 5E E8 " +
                "0E 42 94 4A 6E 7B BA 0D 43 70 CA 00 09 FF 50 19 43 A5 0F 01 FC FF D1 B3 42 16 65 AE 99 F8 2A 43 " +
                "6C C8 00 EA 1D 94 15 43 0C C8 00 BF CB 9F B3 43 1D D2 00 71 EE BA 0D 43 03 C8 00 D9 43 B2 00 43 " +
                "32 C9 00 6C F8 B1 5D 43 08 C8 00 AF E4 2A 8E 43 06 C8 00 E2 CB 2F 1F 43 25 C8 00 66 A1 F0 30 43 " +
                "0F C8 00 2B 04 B4 08 43 2D C9 00 D0 A6 87 E0 43 1A C8 00 0B 97 E0 29 52 C2 08 C9 D2 B8 7F FF 43 " +
                "00 88 00 68 5C CC 6B 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(false, 1);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        String expectedRuns =
            "[data-run vcn:0-80831 cluster:786432]\n" +
            "[data-run vcn:80832-132051 cluster:520176428]\n" +
            "[data-run vcn:132052-203204 cluster:1156359823]\n" +
            "[data-run vcn:203205-233835 cluster:1406469513]\n" +
            "[data-run vcn:233836-252927 cluster:1636794615]\n" +
            "[data-run vcn:252928-304751 cluster:2061533184]\n" +
            "[data-run vcn:304752-374292 cluster:783450108]\n" +
            "[data-run vcn:374293-400170 cluster:1504385450]\n" +
            "[data-run vcn:400171-451478 cluster:1866413972]\n" +
            "[data-run vcn:451479-502690 cluster:585040723]\n" +
            "[data-run vcn:502691-556479 cluster:815395268]\n" +
            "[data-run vcn:556480-607682 cluster:827078045]\n" +
            "[data-run vcn:607683-659188 cluster:2399022601]\n" +
            "[data-run vcn:659189-710396 cluster:489231032]\n" +
            "[data-run vcn:710397-761602 cluster:1012457114]\n" +
            "[data-run vcn:761603-812839 cluster:1833533440]\n" +
            "[data-run vcn:812840-864054 cluster:1979548715]\n" +
            "[data-run vcn:864055-915555 cluster:1451567867]\n" +
            "[data-run vcn:915556-966781 cluster:2154152454]\n" +
            "[data-run vcn:966782-969023 cluster:2004175]\n" +
            "[data-run vcn:969024-1003839 cluster:1810559287]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    @Test
    public void testCompressedExpectingSparseAfterMerge() {
        // Arrange
        byte[] buffer = toByteArray(
            "41 13 D5 68 A2 0B 21 09 68 FF 01 04 00");
        DataRunDecoder dataRunDecoder = new DataRunDecoder(true, 16);

        // Act
        dataRunDecoder.readDataRuns(new NTFSStructure(buffer, 0), 0);
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        String expectedRuns =
            "[data-run vcn:0-15 cluster:195193045]\n" +
            "[compressed-run vcn:16-31 [[data-run vcn:16-18 cluster:195193061], [data-run vcn:19-27 cluster:195192893]]]\n";
        assertDataRuns(dataRuns, expectedRuns);
    }

    /**
     * Asserts the list of data runs is correct.
     *
     * @param dataRuns the data runs to check.
     * @param expected the expected list.
     */
    private void assertDataRuns(List<DataRunInterface> dataRuns, String expected) {
        StringBuilder builder = new StringBuilder();
        for (DataRunInterface dataRun : dataRuns) {
            builder.append(dataRun);
            builder.append('\n');
        }

        String actual = builder.toString();
        assertThat(actual, is(equalTo(expected)));
    }
}

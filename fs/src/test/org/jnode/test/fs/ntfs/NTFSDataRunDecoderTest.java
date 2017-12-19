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
        DataRunDecoder dataRunDecoder = new DataRunDecoder(new NTFSStructure(buffer, 0), 0, false, 1);

        // Act
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
        DataRunDecoder dataRunDecoder = new DataRunDecoder(new NTFSStructure(buffer, 0), 0, false, 1);

        // Act
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
        DataRunDecoder dataRunDecoder = new DataRunDecoder(new NTFSStructure(buffer, 0), 0, true, 16);

        // Act
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        assertThat(dataRunDecoder.getNumberOfVCNs(), is(64));

        String expectedRuns =
            "[compressed-run vcn:0-15 [data-run vcn:0-6 cluster:2228]]\n" +
            "[compressed-run vcn:16-31 [data-run vcn:16-22 cluster:2244]]\n" +
            "[compressed-run vcn:32-47 [data-run vcn:32-38 cluster:2260]]\n" +
            "[compressed-run vcn:48-63 [data-run vcn:48-51 cluster:2276]]\n";
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
        DataRunDecoder dataRunDecoder = new DataRunDecoder(new NTFSStructure(buffer, 0), 0, true, 16);

        // Act
        List<DataRunInterface> dataRuns = dataRunDecoder.getDataRuns();

        // Assert
        assertThat(dataRunDecoder.getNumberOfVCNs(), is(544));

        String expectedRuns =
            "[compressed-run vcn:0-15 [data-run vcn:0-5 cluster:181033]]\n" +
            "[compressed-run vcn:16-31 [data-run vcn:16-21 cluster:181039]]\n" +
            "[compressed-run vcn:32-47 [data-run vcn:32-37 cluster:181045]]\n" +
            "[compressed-run vcn:48-63 [data-run vcn:48-53 cluster:181051]]\n" +
            "[compressed-run vcn:64-79 [data-run vcn:64-69 cluster:181057]]\n" +
            "[compressed-run vcn:80-95 [data-run vcn:80-85 cluster:181063]]\n" +
            "[compressed-run vcn:96-111 [data-run vcn:96-101 cluster:181069]]\n" +
            "[compressed-run vcn:112-127 [data-run vcn:112-117 cluster:181075]]\n" +
            "[compressed-run vcn:128-143 [data-run vcn:128-133 cluster:181081]]\n" +
            "[compressed-run vcn:144-159 [data-run vcn:144-149 cluster:181087]]\n" +
            "[compressed-run vcn:160-175 [data-run vcn:160-165 cluster:181093]]\n" +
            "[compressed-run vcn:176-191 [data-run vcn:176-181 cluster:181099]]\n" +
            "[compressed-run vcn:192-207 [data-run vcn:192-197 cluster:181105]]\n" +
            "[compressed-run vcn:208-223 [data-run vcn:208-213 cluster:181111]]\n" +
            "[compressed-run vcn:224-239 [data-run vcn:224-229 cluster:7186912]]\n" +
            "[compressed-run vcn:240-255 [data-run vcn:240-245 cluster:7192116]]\n" +
            "[compressed-run vcn:256-271 [data-run vcn:256-261 cluster:7195041]]\n" +
            "[compressed-run vcn:272-287 [data-run vcn:272-277 cluster:7945000]]\n" +
            "[compressed-run vcn:288-303 [data-run vcn:288-293 cluster:6022724]]\n" +
            "[compressed-run vcn:304-319 [data-run vcn:304-309 cluster:5305292]]\n" +
            "[compressed-run vcn:320-335 [data-run vcn:320-321 cluster:3088312]]\n" +
            "[data-run vcn:336-527 cluster:7189545]\n" +
            "[compressed-run vcn:528-543 [data-run vcn:528-528 cluster:7189737]]\n";
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

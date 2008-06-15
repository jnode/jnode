package org.jnode.apps.vmware.disk.test.readwrite;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.test.BaseTest;
import org.jnode.apps.vmware.disk.test.Utils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
@RunWith(value = Parameterized.class)
public abstract class BaseReadWriteTest extends BaseTest {
    private static final Logger LOG = Logger.getLogger(BaseReadWriteTest.class);

    private static final String DISKS_PACKAGE = "/org/jnode/apps/vmware/disk/test/disks/";
    private static final String DISK_BASE_NAME = "Menuet32-";

    private static final String RESTRICT_TO_FILE_NAME = "Menuet32-0";
    // private static final String RESTRICT_TO_FILE_NAME = null;

    private static final String DISKS_PATH;
    static {
        String classpath = System.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator, false);
        File foundPath = null;
        while (tokenizer.hasMoreTokens()) {
            File path = new File(tokenizer.nextToken());
            if (path.isDirectory()) {
                path = new File(path, DISKS_PACKAGE);
                if (path.exists()) {
                    foundPath = path;
                    break;
                }
            }
        }
        if (foundPath == null) {
            throw new RuntimeException("directory not found for package " + DISKS_PACKAGE);
        }

        DISKS_PATH = foundPath.getAbsolutePath();

        System.out.println("\ndirectory for package " + DISKS_PACKAGE + " : " + DISKS_PATH);
    }

    @Parameters
    public static List<File[]> data() {
        File directory = new File(DISKS_PATH);
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean ok = name.matches(DISK_BASE_NAME + "[0-9]*.vmdk");

                if (RESTRICT_TO_FILE_NAME != null) {
                    ok &= name.startsWith(RESTRICT_TO_FILE_NAME);
                }

                return ok;
            }
        });
        List<File[]> list = new ArrayList<File[]>(files.length);
        for (File f : files) {
            list.add(new File[] {f});
        }

        return list;
    }

    public BaseReadWriteTest(File diskFile) throws IOException {
        super(diskFile, true);
    }

    @Test
    @Ignore
    public void read() throws Exception {
        VMWareDisk disk = new VMWareDisk(diskFile);

        ByteBuffer data = IOUtils.allocate(IOHandler.SECTOR_SIZE * 100);
        disk.read(0, data);

        Assert.assertEquals(toString() + ": buffer should be filled", 0, data.remaining());
    }

    @Test
    @Ignore
    public void write() throws Exception {
        VMWareDisk disk = new VMWareDisk(diskFile);

        ByteBuffer data = IOUtils.allocate(IOHandler.SECTOR_SIZE * 100);
        disk.write(0, data);

        Assert.assertEquals(toString() + ": buffer should be fully copied", 0, data.remaining());
    }

    @Test
    public void writeAndRead() throws Exception {
        Utils.DO_CLEAR = false;

        LOG.info("BEGIN writeAndRead");
        VMWareDisk disk = new VMWareDisk(diskFile);

        // write
        LOG.info("writeAndRead: writing...");
        int size = IOHandler.SECTOR_SIZE * 100;
        ByteBuffer expectedData = IOUtils.allocate(size);
        for (int i = 0; i < (size / 4); i++) {
            expectedData.putInt(i);
        }
        expectedData.rewind();
        disk.write(0, expectedData);
        disk.flush();

        // read
        LOG.info("writeAndRead: reading...");
        VMWareDisk disk2 = new VMWareDisk(diskFile);
        Assert.assertEquals("disk has different size", disk.getLength(), disk2.getLength());
        Assert.assertEquals("disk has different descriptor", disk.getDescriptor(), disk2
                .getDescriptor());

        expectedData.rewind();
        ByteBuffer actualData = IOUtils.allocate(size);
        disk2.read(0, actualData);
        for (int i = 0; i < (size / 4); i++) {
            int actual = actualData.getInt(i);
            int expected = expectedData.getInt();
            Assert.assertEquals("bad data at index " + (i * 4), expected, actual);
        }
        LOG.info("END   writeAndRead");
    }
}

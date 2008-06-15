package org.jnode.apps.vmware.disk.test;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);

    private static final String TEMP_DIR = "VMWareDisk";
    private static final File TEMP_DIR_FILE;
    static {
        String tmpDir = System.getProperty("java.io.tmpdir");
        TEMP_DIR_FILE = new File(tmpDir, TEMP_DIR);
    }

    private static long SEQ_NUMBER = 0L;

    public static boolean DO_CLEAR = true;

    public static File createTempDir() throws IOException {
        if (!TEMP_DIR_FILE.exists()) {
            if (!TEMP_DIR_FILE.mkdir()) {
                throw new IOException("can't create directory " + TEMP_DIR_FILE.getAbsolutePath());
            }
        } else {
            clearTempDir(false);
        }

        return TEMP_DIR_FILE;
    }

    public static File createTempFile(String prefix) throws IOException {
        File tmpDir = createTempDir();
        return new File(tmpDir, String.valueOf(prefix) + SEQ_NUMBER++);
    }

    public static void clearTempDir(boolean deleteDir) throws IOException {
        if (!DO_CLEAR)
            return;

        if (TEMP_DIR_FILE.exists()) {
            for (File tmpFile : TEMP_DIR_FILE.listFiles()) {
                LOG.debug("deleting file " + tmpFile);
                tmpFile.delete();
            }
        }

        if (deleteDir) {
            TEMP_DIR_FILE.delete();
        }
    }
}

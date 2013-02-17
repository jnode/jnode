/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.apps.vmware.disk.test;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
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

    /**
     * Should we clear the temporary directory ? 
     */
    public static boolean DO_CLEAR = true;

    /**
     * Create a temporary directory that will be used to store VMware disks.
     * @return the temporary directory
     * @throws IOException
     */
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

    /**
     * Create a temporary file in the temporary created (if needed) by {@link #createTempDir()}.
     * @param prefix for the temporary file
     * @return
     * @throws IOException
     */
    public static File createTempFile(String prefix) throws IOException {
        File tmpDir = createTempDir();
        return new File(tmpDir, String.valueOf(prefix) + SEQ_NUMBER++);
    }

    /**
     * Clean the temporary directory.
     * 
     * @param deleteDir true to also delete the temporary directory.
     * @throws IOException
     */
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

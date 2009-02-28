/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.vmware.disk.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

/**
 * Utility methods for copying VMware disks.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class DiskCopier {
    private static final Logger LOG = Logger.getLogger(DiskCopier.class);

    /**
     * Copy a VMware disk to a given directory.
     * @param mainFile main file for the VMware disk.
     * @param toDirectory destination directory for the copied VMware disk.
     * @return the main file for the copied VMware disk 
     * @throws IOException
     */
    public static File copyDisk(final File mainFile, final File toDirectory) throws IOException {
        final String name = mainFile.getName();
        final int idx = name.lastIndexOf('.');
        final String beginName = name.substring(0, idx);
        final String endName = name.substring(idx);

        final File parentDir = mainFile.getParentFile();
        File[] files = parentDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean ok = name.startsWith(beginName) && name.endsWith(endName);
                return ok;
            }
        });

        File mainFileCopy = null;
        for (File file : files) {
            File f = copyFile(file, toDirectory);
            if (file.getName().equals(mainFile.getName())) {
                mainFileCopy = f;
            }
        }

        return mainFileCopy;
    }

    /**
     * Copy a file to a directory.
     * @param file to copy
     * @param toDirectory destination directory
     * @return the copied file
     * @throws IOException
     */
    public static File copyFile(File file, File toDirectory) throws IOException {
        LOG.debug("copying file " + file.getName() + " to " + toDirectory.getName());
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File outFile = null;

        try {
            fis = new FileInputStream(file);
            FileChannel inCh = fis.getChannel();

            outFile = new File(toDirectory, file.getName());
            fos = new FileOutputStream(outFile);
            FileChannel outCh = fos.getChannel();

            outCh.transferFrom(inCh, 0, inCh.size());

            return outFile;
        } catch (IOException ioe) {
            LOG.error(ioe);
            throw ioe;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
    }
}

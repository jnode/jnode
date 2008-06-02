/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.test.fs.filesystem.tests;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.support.TestUtils;

/**
 * This is a test (not a JUnit test !) that create a directory hierarchy
 * with files and data in these files. This test is intented for checking
 * with other OSes (chkdsk/scandisk for windows, fsck for linux ...).
 * This insure some compatibility with the FS of these OSes.
 *
 * @author Fabien DUMINY
 */
public class CheckdiskTest {
    public static final int DIR_LEVEL = 5;
    public static final int FILES_DIR_PER_LEVEL = 2;
    public static final int FILE_SIZE_IN_WORDS = 2 * 1024;

    /**
     * @param config
     * @throws Exception
     */
    public static void makeDisk(FSTestConfig config) throws Exception {
/*
        System.out.println("CheckdiskTest\nCreating empty FileSystem");
        Device wrkDevice = config.getWorkDevice();
        Class fsClass = config.getFsClass();

        System.out.println("Mounting FileSystem");
        FileSystem fs = TestUtils.mountDevice(wrkDevice, fsClass, false);

        FSEntry rootEntry = fs.getRootEntry();
        FSDirectory rootDir = rootEntry.getDirectory();

        createDirectory(rootDir, "TEST");
        createFile(rootDir, "TEST2");
        //createDirAndFiles(rootDir, DIR_LEVEL);

        fs.close();
        System.out.println(
            "FileSystem filled.\nReboot and run your OS checkdisk program (fsck, chkdsk, scandisk ...) " + 
            to complete the test.");
        */
    }

    /**
     * @param parent
     * @param level
     * @throws IOException
     */
    public static void createDirAndFiles(FSDirectory parent, int level) throws IOException {
        for (int i = 0; i < FILES_DIR_PER_LEVEL; i++) {
            FSDirectory subDir = createDirectory(parent, "dir" + i);
            if (level > 0)
                createDirAndFiles(subDir, level - 1);

            createFile(parent, "file_" + level + "_" + i);
        }
    }

    /**
     * @param parent
     * @param dir
     * @return
     * @throws IOException
     */
    public static FSDirectory createDirectory(FSDirectory parent, String dir) throws IOException {
        return parent.addDirectory(dir).getDirectory();
    }

    /**
     * @param parent
     * @param file
     * @return
     * @throws IOException
     */
    public static FSFile createFile(FSDirectory parent, String file) throws IOException {
        FSFile f = parent.addFile(file).getFile();
        ByteBuffer data = ByteBuffer.wrap(TestUtils.getTestData(FILE_SIZE_IN_WORDS));
        f.write(0, data);
        f.flush();
        return f;
    }
}

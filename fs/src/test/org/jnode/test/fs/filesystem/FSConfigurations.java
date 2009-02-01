/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.test.fs.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;
import org.jnode.fs.ext2.BlockSize;
import org.jnode.fs.ext2.Ext2FileSystemFormatter;
import org.jnode.fs.fat.FatFileSystemFormatter;
import org.jnode.fs.fat.FatType;
import org.jnode.test.fs.filesystem.config.DeviceParam;
import org.jnode.test.fs.filesystem.config.FS;
import org.jnode.test.fs.filesystem.config.FSAccessMode;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.config.FSType;
import org.jnode.test.fs.filesystem.config.FileParam;
import org.jnode.test.fs.filesystem.config.OsType;

public class FSConfigurations implements Iterable<FSTestConfig> {
    public static final boolean DO_FORMAT = true;
    public static final boolean DO_NOT_FORMAT = false;

    private List<FSTestConfig> configs = new ArrayList<FSTestConfig>();

    public Iterator<FSTestConfig> iterator() {
        return configs.iterator();
    }

    public FSConfigurations() {
        String tempDir = System.getProperty("java.io.tmpdir");
        final String diskFileName = tempDir + File.separatorChar + "diskimg.WRK";

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.EXT2,
            FSAccessMode.BOTH, new Ext2FileSystemFormatter(BlockSize._1Kb), diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.EXT2,
            FSAccessMode.BOTH, new Ext2FileSystemFormatter(BlockSize._4Kb), diskFileName, "1M"));

//        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.NTFS,
//                FSAccessMode.BOTH, "", DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT,
            FSAccessMode.BOTH, new FatFileSystemFormatter(FatType.FAT12), diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT,
            FSAccessMode.BOTH, new FatFileSystemFormatter(FatType.FAT16), diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT,
            FSAccessMode.BOTH, new FatFileSystemFormatter(FatType.FAT32), diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.JFAT,
                FSAccessMode.BOTH, new FatFileSystemFormatter(FatType.FAT32), diskFileName, "1M"));

//        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.ISO9660,
//                FSAccessMode.BOTH, "", DO_FORMAT, diskFileName, "1M"));

/*
        configs.addAll(createConfigs(OsType.JNODE_OS, FSType.EXT2,
                FSAccessMode.BOTH, "1", DO_FORMAT));
        //<workDevice name="hdb5" />

        configs.addAll(createConfigs(OsType.JNODE_OS, FSType.EXT2,
                FSAccessMode.BOTH, null, DO_NOT_FORMAT));
        //<workRamdisk size="1M" />
*/
    }

    private List<FSTestConfig> createFileConfigs(OsType osType, FSType fsType,
                                                 FSAccessMode accessMode, Formatter<? extends FileSystem<?>> formatter,
                                                 String fileName, String fileSize) {
        FileParam fp = new FileParam(fileName, fileSize);
        return createConfigs(osType, fsType,
            accessMode, formatter, fp);
    }

    private List<FSTestConfig> createConfigs(OsType osType, FSType fsType,
                                             FSAccessMode accessMode, Formatter<? extends FileSystem<?>> formatter,
                                             DeviceParam device) {
        List<FSTestConfig> configs = new ArrayList<FSTestConfig>();

        if (osType.isCurrentOS()) {
            if (accessMode.doReadOnlyTests()) {
                // true=readOnly mode
                FS fs = new FS(fsType, true, formatter);

                FSTestConfig cfg = new FSTestConfig(osType, fs, device);
                configs.add(cfg);
            }

            if (accessMode.doReadWriteTests()) {
                // false=readWrite mode
                FS fs = new FS(fsType, false, formatter);

                FSTestConfig cfg = new FSTestConfig(osType, fs, device);
                configs.add(cfg);
            }
        }

        return configs;
    }
}

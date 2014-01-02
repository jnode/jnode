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

package org.jnode.test.fs.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;
import org.jnode.fs.ext2.BlockSize;
import org.jnode.fs.ext2.Ext2FileSystemFormatter;
import org.jnode.fs.fat.FatType;
import org.jnode.test.fs.filesystem.config.FS;
import org.jnode.test.fs.filesystem.config.FSAccessMode;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.config.FSType;
import org.jnode.test.fs.filesystem.config.FileParam;
import org.jnode.test.fs.filesystem.config.OsType;

public class FSConfigurations implements Iterable<FSTestConfig> {

    public static final String DISK_FILE_NAME =
        System.getProperty("java.io.tmpdir") + File.separatorChar + "diskimg.WRK";

    private List<FSTestConfig> configs = new ArrayList<FSTestConfig>();

    public Iterator<FSTestConfig> iterator() {
        return configs.iterator();
    }

    public FSConfigurations() {

        FileSystemTestConfigurationListBuilder defaultBuilder =
            new FileSystemTestConfigurationListBuilder().osType(OsType.OTHER_OS).accessMode(FSAccessMode.BOTH)
                .filename(DISK_FILE_NAME).fileSize("1M");

        FileSystemTestConfigurationListBuilder fatBuilder =
            new FileSystemTestConfigurationListBuilder().osType(OsType.OTHER_OS).accessMode(FSAccessMode.BOTH)
                .filename(DISK_FILE_NAME).fileSize("33M");

        configs
            .addAll(defaultBuilder.fsType(FSType.EXT2).formatter(new Ext2FileSystemFormatter(BlockSize._1Kb)).build());
        configs
            .addAll(defaultBuilder.fsType(FSType.EXT2).formatter(new Ext2FileSystemFormatter(BlockSize._4Kb)).build());
        /* TODO Fix HFS failures
        configs.addAll(
            defaultBuilder.fsType(FSType.HFS_PLUS).formatter(new HfsPlusFileSystemFormatter(new HFSPlusParams()))
                .build());*/

        /* TODO Implement FAT12 writing
        configs.addAll(
            fatBuilder.fsType(FSType.FAT).formatter(new org.jnode.fs.fat.FatFileSystemFormatter(FatType.FAT12))
                .build());*/
        configs.addAll(
            fatBuilder.fsType(FSType.FAT).formatter(new org.jnode.fs.fat.FatFileSystemFormatter(FatType.FAT16))
                .build());
        configs.addAll(
            fatBuilder.fsType(FSType.FAT).formatter(new org.jnode.fs.fat.FatFileSystemFormatter(FatType.FAT32))
                .build());
        /* TODO Implement FAT12 writing
        configs.addAll(fatBuilder.fsType(FSType.JFAT).formatter(new FatFileSystemFormatter(ClusterSize._64Kb)).build());
        */
    }

    class FileSystemTestConfigurationListBuilder {

        private OsType osType;
        private FSType fsType;
        private FSAccessMode fsAccessMode;
        private Formatter<? extends FileSystem<?>> formatter;
        private String fileName;
        private String fileSize;


        public FileSystemTestConfigurationListBuilder osType(OsType osType) {
            this.osType = osType;
            return this;
        }

        public FileSystemTestConfigurationListBuilder fsType(FSType fsType) {
            this.fsType = fsType;
            return this;
        }

        public FileSystemTestConfigurationListBuilder accessMode(FSAccessMode accessMode) {
            this.fsAccessMode = accessMode;
            return this;
        }

        public FileSystemTestConfigurationListBuilder formatter(Formatter<? extends FileSystem<?>> formatter) {
            this.formatter = formatter;
            return this;
        }

        public FileSystemTestConfigurationListBuilder filename(String filename) {
            this.fileName = filename;
            return this;
        }

        public FileSystemTestConfigurationListBuilder fileSize(String fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public List<FSTestConfig> build() {
            FileParam fp = new FileParam(fileName, fileSize);
            List<FSTestConfig> configurationList = new ArrayList<FSTestConfig>();
            if (osType.isCurrentOS()) {
                FS fs;
                if (fsAccessMode.doReadOnlyTests()) {
                    // true=readOnly mode
                    fs = new FS(fsType, true, formatter);
                    configurationList.add(new FSTestConfig(osType, fs, fp));
                }

                if (fsAccessMode.doReadWriteTests()) {
                    // false=readWrite mode
                    fs = new FS(fsType, false, formatter);
                    configurationList.add(new FSTestConfig(osType, fs, fp));
                }
            }

            return configurationList;
        }
    }
}

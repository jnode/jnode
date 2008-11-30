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

package org.jnode.test.fs.filesystem.config;

import java.io.IOException;
import javax.naming.NameNotFoundException;
import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemType;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.FatFileSystemType;
import org.jnode.fs.iso9660.ISO9660FileSystem;
import org.jnode.fs.iso9660.ISO9660FileSystemType;
import org.jnode.fs.ntfs.NTFSFileSystem;
import org.jnode.fs.ntfs.NTFSFileSystemType;

/**
 * @author Fabien DUMINY
 */
public enum FSType {
    EXT2("ext2", Ext2FileSystem.class, Ext2FileSystemType.class,
        new String[]{".", "..", "lost+found"}),
    FAT("fat", FatFileSystem.class, FatFileSystemType.class,
        null),
    JFAT("jfat", org.jnode.fs.jfat.FatFileSystem.class, org.jnode.fs.jfat.FatFileSystemType.class,
            null),

    ISO9660("iso9660", ISO9660FileSystem.class, ISO9660FileSystemType.class,
        new String[]{".", ".."}),
    NTFS("ntfs", NTFSFileSystem.class, NTFSFileSystemType.class,
        new String[]{"."});

    private final Class<? extends FileSystem> fsClass;

    private final Class<? extends FileSystemType> fsTypeClass;

    private final String name;
    private final String[] emptyDirNames;

    private FSType(String name, Class<? extends FileSystem> fsClass,
                   Class<? extends FileSystemType> fsTypeClass,
                   String[] emptyDirNames) {
        this.name = name;
        this.fsClass = fsClass;
        this.fsTypeClass = fsTypeClass;
        this.emptyDirNames = emptyDirNames;
    }

    public String[] getEmptyDirNames(boolean isRoot) {
        return emptyDirNames;
    }

    public FileSystem mount(Device device, boolean readOnly)
        throws IOException, FileSystemException, NameNotFoundException, InstantiationException, IllegalAccessException {

        // mount the device
        FileSystemType type = (FileSystemType) fsTypeClass.newInstance();
        FileSystem fs = type.create(device, readOnly);

        return fs;
    }

    /**
     * @return
     */
    public Class<? extends FileSystem> getFsClass() {
        return fsClass;
    }

    /**
     * @return
     */
    public Class<? extends FileSystemType> getFsTypeClass() {
        return fsTypeClass;
    }

    /**
     *
     */
    public String toString() {
        return name;
    }
}

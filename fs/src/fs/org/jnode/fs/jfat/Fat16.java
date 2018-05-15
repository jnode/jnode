/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.jfat;

import java.io.IOException;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * A FAT implementation for FAT-16.
 *
 * @author Luke Quinane
 */
public class Fat16 extends Fat {
    public Fat16(BootSector bs, BlockDeviceAPI api) {
        super(bs, api);
    }

    protected long offset(int index) {
        return (long) (2 * index);
    }

    public int get(int index) throws IOException {
        return (int) getUInt16(index);
    }

    public int set(int index, int element) throws IOException {
        long old = getUInt16(index);

        setInt16(index, element & 0xFFFF);

        return (int) (old & 0x0000FFFF);
    }

    public long getClusterPosition(int index) {
        BootSector bootSector = getBootSector();

        long rootDirectoryOffset = bootSector.getFirstDataSector() * bootSector.getBytesPerSector();

        if (index == 0) {
            return rootDirectoryOffset;
        }

        // Need to account for the size of the root directory entry for following clusters
        long filesOffset = rootDirectoryOffset + bootSector.getNrRootDirEntries() * 32;
        return filesOffset + ((index - firstCluster()) * getClusterSize());
    }

    @Override
    public boolean hasNext(int entry) {
        return !isEofChain(entry);
    }

    public boolean isEofChain(int entry) {
        return (entry >= 0xFFF8);
    }

    public int eofChain() {
        return 0xFFF8;
    }
}

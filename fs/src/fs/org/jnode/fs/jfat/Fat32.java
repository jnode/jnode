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
 * @author gvt
 */
public class Fat32 extends Fat {
    public Fat32(BootSector bs, BlockDeviceAPI api) {
        super(bs, api);
    }

    protected long offset(int index) {
        return (long) (4 * index);
    }

    public long getClusterPosition(int index) {
        return getClusterSector(index) * (long) getBootSector().getBytesPerSector();
    }

    public int get(int index) throws IOException {
        return (int) (getUInt32(index) & 0x0FFFFFFF);
    }

    public int set(int index, int element) throws IOException {
        long old = getUInt32(index);

        setInt32(index, (int) ((element & 0x0FFFFFFF) | (old & 0xF0000000)));

        return (int) (old & 0x0FFFFFFF);
    }

    public boolean isEofChain(int entry) {
        return (entry >= 0x0FFFFFF8);
    }

    public int eofChain() {
        return 0x0FFFFFFF;
    }
}

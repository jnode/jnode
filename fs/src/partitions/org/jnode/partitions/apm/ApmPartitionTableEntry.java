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

package org.jnode.partitions.apm;

import java.nio.charset.Charset;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.util.BigEndian;
import org.jnode.util.NumberUtils;

/**
 * A APM partition table entry.
 *
 * @author Luke Quinane
 */
public class ApmPartitionTableEntry implements PartitionTableEntry {

    /**
     * The first 16KiB of the drive.
     */
    private final byte[] first16KiB;

    /**
     * The offset to this partition table entry.
     */
    private final int offset;

    /**
     * Creates a new entry.
     *
     * @param parent     the parent table.
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param offset     the offset of this entry in the table.
     */
    public ApmPartitionTableEntry(ApmPartitionTable parent, byte[] first16KiB, int offset) {
        this.first16KiB = first16KiB;
        this.offset = offset;
    }

    @Override
    public boolean isValid() {
        return first16KiB.length > offset + 128;
    }

    /**
     * @see org.jnode.partitions.PartitionTableEntry#getChildPartitionTable()
     */
    @Override
    public IBMPartitionTable getChildPartitionTable() {
        throw new UnsupportedOperationException("No child partitions.");
    }

    /**
     * @see org.jnode.partitions.PartitionTableEntry#hasChildPartitionTable()
     */
    @Override
    public boolean hasChildPartitionTable() {
        return false;
    }

    public long getStartOffset() {
        return BigEndian.getUInt32(first16KiB, offset + 0x8) * 0x200L;
    }

    public long getEndOffset() {
        return getStartOffset() + BigEndian.getUInt32(first16KiB, offset + 0xc) * 0x200L;
    }

    public String getName() {
        byte[] nameBytes = new byte[31];
        System.arraycopy(first16KiB, offset + 0x10, nameBytes, 0, nameBytes.length);
        return new String(nameBytes, Charset.forName("ASCII")).replace("\u0000", "");
    }

    public String getType() {
        byte[] nameBytes = new byte[31];
        System.arraycopy(first16KiB, offset + 0x30, nameBytes, 0, nameBytes.length);
        return new String(nameBytes, Charset.forName("ASCII")).replace("\u0000", "");
    }

    public String dump() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            b.append(NumberUtils.hex(BigEndian.getUInt8(first16KiB, offset + i), 2));
            b.append(' ');
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        builder.append('[').append(getName()).append(' ');
        builder.append("t:").append(getType()).append(' ');
        builder.append("s:").append(getStartOffset()).append(' ');
        builder.append("e:").append(getEndOffset()).append(']');
        return builder.toString();
    }
}

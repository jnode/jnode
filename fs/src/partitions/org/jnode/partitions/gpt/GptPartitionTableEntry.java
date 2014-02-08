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
 
package org.jnode.partitions.gpt;

import java.nio.charset.Charset;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.LittleEndian;
import org.jnode.util.NumberUtils;

/**
 * A GPT partition table entry.
 *
 * @author Luke Quinane
 */
public class GptPartitionTableEntry implements PartitionTableEntry {

    /** The first 16KiB of the drive. */
    private final byte[] first16KiB;

    /** The block size. */
    private int blockSize;

    /** The offset to this partition table entry. */
    private final int offset;

    /**
     * Creates a new entry.
     *
     * @param parent the parent table.
     * @param first16KiB the first 16,384 bytes of the disk.
     * @param offset the offset of this entry in the table.
     * @param blockSize the block size.
     */
    public GptPartitionTableEntry(GptPartitionTable parent, byte[] first16KiB, int offset, int blockSize) {
        this.first16KiB = first16KiB;
        this.blockSize = blockSize;
        this.offset = offset;
    }

    @Override
    public boolean isValid() {
        return first16KiB.length > offset + 128 && !isEmpty();
    }

    /**
     * @see org.jnode.partitions.PartitionTableEntry#getChildPartitionTable()
     */
    @Override
    public PartitionTable<?> getChildPartitionTable() {
        throw new UnsupportedOperationException("No child partitions.");
    }

    /**
     * @see org.jnode.partitions.PartitionTableEntry#hasChildPartitionTable()
     */
    @Override
    public boolean hasChildPartitionTable() {
        return false;
    }

    public boolean isEmpty() {
        return GptPartitionTypes.lookUp(getPartitionTypeGuid()) == GptPartitionTypes.UNUSED_ENTRY;
    }

    public byte[] getPartitionTypeGuid() {
        byte[] guid = new byte[16];
        System.arraycopy(first16KiB, offset, guid, 0, guid.length);
        return guid;
    }

    public byte[] getPartitionGuid() {
        byte[] guid = new byte[16];
        System.arraycopy(first16KiB, offset + 0x10, guid, 0, guid.length);
        return guid;
    }

    public long getStartOffset() {
        return LittleEndian.getInt64(first16KiB, offset + 0x20) * blockSize;
    }

    public long getEndOffset() {
        return (LittleEndian.getInt64(first16KiB, offset + 0x28) + 1) * blockSize;
    }

    public long getFlags() {
        return LittleEndian.getInt64(first16KiB, offset + 0x30);
    }

    public String getName() {
        byte[] nameBytes = new byte[72];
        System.arraycopy(first16KiB, offset + 0x38, nameBytes, 0, nameBytes.length);
        String rawName = new String(nameBytes, Charset.forName("UTF-16LE"));

        if (rawName.contains("\u0000")) {
            return rawName.substring(0, rawName.indexOf("\u0000"));
        }

        return rawName;
    }

    public String dump() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            b.append(NumberUtils.hex(LittleEndian.getUInt8(first16KiB, offset + i), 2));
            b.append(' ');
        }
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        builder.append('[').append(getName()).append(' ');
        builder.append(NumberUtils.hex(getPartitionTypeGuid())).append(' ');
        builder.append(NumberUtils.hex(getPartitionGuid())).append(' ');
        builder.append("s:").append(getStartOffset()).append(' ');
        builder.append("e:").append(getEndOffset()).append(']');
        return builder.toString();
    }
}

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
 
package org.jnode.partitions.ibm;

import org.apache.log4j.Logger;
import org.jnode.driver.block.CHS;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.LittleEndian;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class IBMPartitionTableEntry implements PartitionTableEntry {
    private static final int BOOTABLE = 0x80;

    private final Logger log = Logger.getLogger(getClass());

    private final byte[] bs;
    private final int ofs;
    private long odd;
    @SuppressWarnings("unused")
    private final IBMPartitionTable parent;

    public IBMPartitionTableEntry(IBMPartitionTable parent, byte[] bs, int partNr) {
        this.parent = parent;
        this.bs = bs;
        this.ofs = 446 + (partNr * 16);
    }

    public boolean isValid() {
        int bootIndicatorValue = getBootIndicatorValue();

        return
            !isEmpty() &&
            (bootIndicatorValue == 0 || bootIndicatorValue == BOOTABLE) &&
            getNrSectors() > 0;
    }

    /**
     * @see org.jnode.partitions.PartitionTableEntry#getChildPartitionTable()
     */
    public IBMPartitionTable getChildPartitionTable() {
        throw new Error("Not implemented yet");
    }

    /**
     * @see org.jnode.partitions.PartitionTableEntry#hasChildPartitionTable()
     */
    public boolean hasChildPartitionTable() {
        return isExtended();
    }

    public boolean isEmpty() {
        return (getSystemIndicator() == IBMPartitionTypes.PARTTYPE_EMPTY);
    }

    public boolean isExtended() {
        final IBMPartitionTypes id = getSystemIndicator();
        // pgwiasda
        // there are more than one type of extended Partitions
        return
            id == IBMPartitionTypes.PARTTYPE_WIN95_FAT32_EXTENDED ||
            id == IBMPartitionTypes.PARTTYPE_LINUX_EXTENDED ||
            id == IBMPartitionTypes.PARTTYPE_DOS_EXTENDED;
    }

    public boolean getBootIndicator() {
        return getBootIndicatorValue() == BOOTABLE;
    }

    public int getBootIndicatorValue() {
        return LittleEndian.getUInt8(bs, ofs + 0);
    }

    public void setBootIndicator(boolean active) {
        LittleEndian.setInt8(bs, ofs + 0, (active) ? BOOTABLE : 0);
    }

    public CHS getStartCHS() {
        int v1 = LittleEndian.getUInt8(bs, ofs + 1);
        int v2 = LittleEndian.getUInt8(bs, ofs + 2);
        int v3 = LittleEndian.getUInt8(bs, ofs + 3);
        /*
         * h = byte1; s = byte2 & 0x3f; c = ((byte2 & 0xc0) << 2) + byte3;
         */
        return new CHS(((v2 & 0xc0) << 2) + v3, v1, v2 & 0x3f);
    }

    public void setStartCHS(CHS chs) {
        LittleEndian.setInt8(bs, ofs + 1, Math.min(1023, chs.getHead()));
        LittleEndian.setInt8(bs, ofs + 2, ((chs.getCylinder() >> 2) & 0xC0) +
                (chs.getSector() & 0x3f));
        LittleEndian.setInt8(bs, ofs + 3, chs.getCylinder() & 0xFF);
    }

    public int getSystemIndicatorCode() {
        return LittleEndian.getUInt8(bs, ofs + 4);
    }

    public IBMPartitionTypes getSystemIndicator() {
        int code = getSystemIndicatorCode();
        try {
            return IBMPartitionTypes.valueOf(code);
        } catch (IllegalArgumentException e) {
            log.debug("Unknown or invalid system indicator code: 0x" + Integer.toHexString(code));
            return IBMPartitionTypes.PARTTYPE_UNKNOWN;
        }
    }

    public void setSystemIndicator(IBMPartitionTypes type) {
        LittleEndian.setInt8(bs, ofs + 4, type.getCode());
    }

    public CHS getEndCHS() {
        int v1 = LittleEndian.getUInt8(bs, ofs + 5);
        int v2 = LittleEndian.getUInt8(bs, ofs + 6);
        int v3 = LittleEndian.getUInt8(bs, ofs + 7);
        /*
         * h = byte1; s = byte2 & 0x3f; c = ((byte2 & 0xc0) << 2) + byte3;
         */
        return new CHS(((v2 & 0xc0) << 2) + v3, v1, v2 & 0x3f);
    }

    public void setEndCHS(CHS chs) {
        LittleEndian.setInt8(bs, ofs + 5, chs.getHead());
        LittleEndian.setInt8(bs, ofs + 6, ((chs.getCylinder() >> 2) & 0xC0) +
                (chs.getSector() & 0x3f));
        LittleEndian.setInt8(bs, ofs + 7, chs.getCylinder() & 0xFF);
    }

    public long getStartLba() {
        return LittleEndian.getUInt32(bs, ofs + 8);
    }

    public void setStartLba(long v) {
        LittleEndian.setInt32(bs, ofs + 8, (int) v);
    }

    public long getNrSectors() {
        return LittleEndian.getUInt32(bs, ofs + 12);
    }

    public void setNrSectors(long v) {
        LittleEndian.setInt32(bs, ofs + 12, (int) v);
    }

    public long getNbrBlocks(int sectorSize) {
        long sectors = getNrSectors();
        long blocks = sectors;
        if (sectorSize < 1024) {
            blocks /= (1024 / sectorSize);
            odd = getNrSectors() % (1024 / sectorSize);
        } else {
            blocks *= (sectorSize / 1024);
        }
        return blocks;
    }

    public boolean isOdd() {
        return odd != 0;
    }

    public void clear() {
        for (int i = 0; i < 16; i++) {
            LittleEndian.setInt8(bs, ofs + i, 0);
        }
    }

    public String dump() {
        StringBuilder b = new StringBuilder(64);
        for (int i = 0; i < 16; i++) {
            b.append(NumberUtils.hex(LittleEndian.getUInt8(bs, ofs + i), 2));
            b.append(' ');
        }
        return b.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder b = new StringBuilder(32);
        b.append('[').append(getBootIndicator() ? 'A' : '-').append(' ');
        b.append(NumberUtils.hex(getSystemIndicatorCode(), 2)).append(" \'");
        b.append(getSystemIndicator().getName()).append("\' ");
        b.append("s:").append(getStartLba()).append(' ');
        b.append("e:").append(getStartLba() + getNrSectors() - 1).append(']');
        return b.toString();
    }

}

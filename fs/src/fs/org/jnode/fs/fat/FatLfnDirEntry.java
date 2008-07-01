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
 
package org.jnode.fs.fat;

import org.jnode.util.LittleEndian;

/**
 * @author gbin
 */
public class FatLfnDirEntry extends FatBasicDirEntry {
    /**
     * @param dir
     */
    public FatLfnDirEntry(AbstractDirectory dir) {
        super(dir);
    }

    /**
     * @param dir
     * @param src
     * @param offset
     */
    public FatLfnDirEntry(AbstractDirectory dir, byte[] src, int offset) {
        super(dir, src, offset);
    }

    public FatLfnDirEntry(AbstractDirectory dir, String subName, int ordinal, byte checkSum,
            boolean isLast) {
        super(dir);
        char[] unicodechar = new char[13];
        subName.getChars(0, subName.length(), unicodechar, 0);
        if (isLast) {
            LittleEndian.setInt8(rawData, 0, ordinal + (1 << 6)); // set the
                                                                    // 6th
            // security ending
            // bit
        } else {
            LittleEndian.setInt8(rawData, 0, ordinal);
        }

        LittleEndian.setInt16(rawData, 1, unicodechar[0]);
        LittleEndian.setInt16(rawData, 3, unicodechar[1]);
        LittleEndian.setInt16(rawData, 5, unicodechar[2]);
        LittleEndian.setInt16(rawData, 7, unicodechar[3]);
        LittleEndian.setInt16(rawData, 9, unicodechar[4]);
        LittleEndian.setInt8(rawData, 11, 0x0f); // this is the hidden
                                                    // attribute tag for
        // lfn
        LittleEndian.setInt8(rawData, 12, 0); // reserved
        LittleEndian.setInt8(rawData, 13, checkSum); // checksum
        LittleEndian.setInt16(rawData, 14, unicodechar[5]);
        LittleEndian.setInt16(rawData, 16, unicodechar[6]);
        LittleEndian.setInt16(rawData, 18, unicodechar[7]);
        LittleEndian.setInt16(rawData, 20, unicodechar[8]);
        LittleEndian.setInt16(rawData, 22, unicodechar[9]);
        LittleEndian.setInt16(rawData, 24, unicodechar[10]);
        LittleEndian.setInt16(rawData, 26, 0); // sector... unused
        LittleEndian.setInt16(rawData, 28, unicodechar[11]);
        LittleEndian.setInt16(rawData, 30, unicodechar[12]);

    }

    public byte getOrdinal() {
        return (byte) LittleEndian.getUInt8(rawData, 0);
    }

    public byte getCheckSum() {
        return (byte) LittleEndian.getUInt8(rawData, 13);
    }

    public String getSubstring() {
        char[] unicodechar = new char[13];
        unicodechar[0] = (char) LittleEndian.getUInt16(rawData, 1);
        unicodechar[1] = (char) LittleEndian.getUInt16(rawData, 3);
        unicodechar[2] = (char) LittleEndian.getUInt16(rawData, 5);
        unicodechar[3] = (char) LittleEndian.getUInt16(rawData, 7);
        unicodechar[4] = (char) LittleEndian.getUInt16(rawData, 9);
        unicodechar[5] = (char) LittleEndian.getUInt16(rawData, 14);
        unicodechar[6] = (char) LittleEndian.getUInt16(rawData, 16);
        unicodechar[7] = (char) LittleEndian.getUInt16(rawData, 18);
        unicodechar[8] = (char) LittleEndian.getUInt16(rawData, 20);
        unicodechar[9] = (char) LittleEndian.getUInt16(rawData, 22);
        unicodechar[10] = (char) LittleEndian.getUInt16(rawData, 24);
        unicodechar[11] = (char) LittleEndian.getUInt16(rawData, 28);
        unicodechar[12] = (char) LittleEndian.getUInt16(rawData, 30);
        int index = 0;
        while (index < 13 && unicodechar[index] != '\0')
            index++;
        return new String(unicodechar).substring(0, index);
    }

    public String toString() {
        return "LFN ordinal " + getOrdinal() + " subString = " + getSubstring() + "CheckSum = " +
                getCheckSum();
    }
}

/*
 * $Id$
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
 
package org.jnode.vm;

import org.jnode.system.MemoryScanner;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class MemoryScannerImpl implements MemoryScanner {

    /**
     * @see org.jnode.system.MemoryScanner#findInt16(org.jnode.vm.Address, int, int, int)
     */
    public Address findInt16(Address start, int size, int match, int stepSize) {
        int offset = 0;
        match &= 0xFFFF;
        size -= 1;
        while (offset < size) {
            if ((start.loadShort(Offset.fromIntSignExtend(offset)) & 0xFFFF) == match) {
                return start.add(offset);
            } else {
                offset += stepSize;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt32(org.jnode.vm.Address, int, int, int)
     */
    public Address findInt32(Address start, int size, int match, int stepSize) {
        int offset = 0;
        size -= 3;
        while (offset < size) {
            if (start.loadInt(Offset.fromIntSignExtend(offset)) == match) {
                return start.add(offset);
            } else {
                offset += stepSize;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt64(org.jnode.vm.Address, int, long, int)
     */
    public Address findInt64(Address start, int size, long match, int stepSize) {
        int offset = 0;
        size -= 7;
        while (offset < size) {
            if (start.loadLong(Offset.fromIntSignExtend(offset)) == match) {
                return start.add(offset);
            } else {
                offset += stepSize;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt8(org.jnode.vm.Address, int, int, int)
     */
    public Address findInt8(Address start, int size, int match, int stepSize) {
        int offset = 0;
        match &= 0xFF;
        while (offset < size) {
            if ((start.loadByte(Offset.fromIntSignExtend(offset)) & 0xFF) == match) {
                return start.add(offset);
            } else {
                offset += stepSize;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt8Array(org.jnode.vm.Address, int, byte[], int, int, int)
     */
    public Address findInt8Array(Address start, int size, byte[] match,
                                 int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= (matchLength - 1);
        final int match0 = match[matchOffset] & 0xFF;
        while (offset < size) {
            if ((start.loadByte(Offset.fromIntSignExtend(offset)) & 0xFF) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return start.add(offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt16Array(org.jnode.vm.Address, int, char[], int, int, int)
     */
    public Address findInt16Array(Address start, int size, char[] match,
                                  int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 2) - 1);
        final int match0 = match[matchOffset] & 0xFFFF;
        while (offset < size) {
            if ((start.loadChar(Offset.fromIntSignExtend(offset)) & 0xFFFF) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return start.add(offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt16Array(org.jnode.vm.Address, int, short[], int, int, int)
     */
    public Address findInt16Array(Address start, int size, short[] match,
                                  int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 2) - 1);
        final int match0 = match[matchOffset] & 0xFFFF;
        while (offset < size) {
            if ((start.loadShort(Offset.fromIntSignExtend(offset)) & 0xFFFF) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return start.add(offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt32Array(org.jnode.vm.Address, int, int[], int, int, int)
     */
    public Address findInt32Array(Address start, int size, int[] match,
                                  int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 4) - 1);
        final int match0 = match[matchOffset];
        while (offset < size) {
            if (start.loadInt(Offset.fromIntSignExtend(offset)) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return start.add(offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }

    /**
     * @see org.jnode.system.MemoryScanner#findInt64Array(org.jnode.vm.Address, int, long[], int, int, int)
     */
    public Address findInt64Array(Address start, int size, long[] match,
                                  int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 8) - 1);
        final long match0 = match[matchOffset];
        while (offset < size) {
            if (start.loadLong(Offset.fromIntSignExtend(offset)) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return start.add(offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }

    private final boolean isMatch(Address start, int offset, byte[] match,
                                  int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if ((start.loadByte(Offset.fromIntSignExtend(offset + i)) & 0xFF) != (match[matchOffset + i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    private final boolean isMatch(Address start, int offset, char[] match,
                                  int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if ((start.loadChar(Offset.fromIntSignExtend(offset + (i * 2))) & 0xFFFF) !=
                (match[matchOffset + i] & 0xFFFF)) {
                return false;
            }
        }
        return true;
    }

    private final boolean isMatch(Address start, int offset, short[] match,
                                  int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if ((start.loadShort(Offset.fromIntSignExtend(offset + (i * 2))) & 0xFFFF) !=
                (match[matchOffset + i] & 0xFFFF)) {
                return false;
            }
        }
        return true;
    }

    private final boolean isMatch(Address start, int offset, int[] match,
                                  int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if (start.loadInt(Offset.fromIntSignExtend(offset + (i * 4))) != match[matchOffset + i]) {
                return false;
            }
        }
        return true;
    }

    private final boolean isMatch(Address start, int offset, long[] match,
                                  int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if (start.loadLong(Offset.fromIntSignExtend(offset + (i * 8))) != match[matchOffset + i]) {
                return false;
            }
        }
        return true;
    }
}

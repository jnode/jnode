/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.jnode.vm;

import org.jnode.system.MemoryScanner;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MemoryScannerImpl implements MemoryScanner {

    /**
     * @see org.jnode.system.MemoryScanner#findInt16(org.jnode.vm.Address, int, int, int)
     */
    public VmAddress findInt16(VmAddress start, int size, int match, int stepSize) {
        int offset = 0;
        match &= 0xFFFF;
        size -= 1;
        while (offset < size) {
            if ((Unsafe.getShort(start, offset) & 0xFFFF) == match) {
                return Unsafe.add(start, offset);
            } else {
                offset += stepSize;
            }           
        }
        return null;
    }
    /**
     * @see org.jnode.system.MemoryScanner#findInt32(org.jnode.vm.Address, int, int, int)
     */
    public VmAddress findInt32(VmAddress start, int size, int match, int stepSize) {
        int offset = 0;
        size -= 3;
        while (offset < size) {
            if (Unsafe.getInt(start, offset) == match) {
                return Unsafe.add(start, offset);
            } else {
                offset += stepSize;
            }           
        }
        return null;
    }
    /**
     * @see org.jnode.system.MemoryScanner#findInt64(org.jnode.vm.Address, int, long, int)
     */
    public VmAddress findInt64(VmAddress start, int size, long match, int stepSize) {
        int offset = 0;
        size -= 7;
        while (offset < size) {
            if (Unsafe.getLong(start, offset) == match) {
                return Unsafe.add(start, offset);
            } else {
                offset += stepSize;
            }           
        }
        return null;
    }
    /**
     * @see org.jnode.system.MemoryScanner#findInt8(org.jnode.vm.Address, int, int, int)
     */
    public VmAddress findInt8(VmAddress start, int size, int match, int stepSize) {
        int offset = 0;
        match &= 0xFF;
        while (offset < size) {
            if ((Unsafe.getByte(start, offset) & 0xFF) == match) {
                return Unsafe.add(start, offset);
            } else {
                offset += stepSize;
            }           
        }
        return null;
    }
    
    /**
     * @see org.jnode.system.MemoryScanner#findInt8Array(org.jnode.vm.Address, int, byte[], int, int, int)
     */
    public VmAddress findInt8Array(VmAddress start, int size, byte[] match,
            int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= (matchLength - 1);
        final int match0 = match[matchOffset] & 0xFF;
        while (offset < size) {            
            if ((Unsafe.getByte(start, offset) & 0xFF) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return Unsafe.add(start, offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }
    
    /**
     * @see org.jnode.system.MemoryScanner#findInt16Array(org.jnode.vm.Address, int, char[], int, int, int)
     */
    public VmAddress findInt16Array(VmAddress start, int size, char[] match,
            int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 2) - 1);
        final int match0 = match[matchOffset] & 0xFFFF;
        while (offset < size) {            
            if ((Unsafe.getChar(start, offset) & 0xFFFF) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return Unsafe.add(start, offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }
    
    /**
     * @see org.jnode.system.MemoryScanner#findInt16Array(org.jnode.vm.Address, int, short[], int, int, int)
     */
    public VmAddress findInt16Array(VmAddress start, int size, short[] match,
            int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 2) - 1);
        final int match0 = match[matchOffset] & 0xFFFF;
        while (offset < size) {            
            if ((Unsafe.getShort(start, offset) & 0xFFFF) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return Unsafe.add(start, offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }
    
    /**
     * @see org.jnode.system.MemoryScanner#findInt32Array(org.jnode.vm.Address, int, int[], int, int, int)
     */
    public VmAddress findInt32Array(VmAddress start, int size, int[] match,
            int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 4) - 1);
        final int match0 = match[matchOffset];
        while (offset < size) {            
            if (Unsafe.getInt(start, offset) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return Unsafe.add(start, offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }
    
    /**
     * @see org.jnode.system.MemoryScanner#findInt64Array(org.jnode.vm.Address, int, long[], int, int, int)
     */
    public VmAddress findInt64Array(VmAddress start, int size, long[] match,
            int matchOffset, int matchLength, int stepSize) {
        int offset = 0;
        size -= ((matchLength * 8) - 1);
        final long match0 = match[matchOffset];
        while (offset < size) {            
            if (Unsafe.getLong(start, offset) == match0) {
                if (isMatch(start, offset, match, matchOffset, matchLength)) {
                    return Unsafe.add(start, offset);
                }
            }
            offset += stepSize;
        }
        return null;
    }
    
    private final boolean isMatch(VmAddress start, int offset, byte[] match,
            int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if ((Unsafe.getByte(start, offset + i) & 0xFF) != (match[matchOffset+i] & 0xFF)) {
                return false;
            }            
        }
        return true;
    }

    private final boolean isMatch(VmAddress start, int offset, char[] match,
            int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if ((Unsafe.getChar(start, offset + (i * 2)) & 0xFFFF) != (match[matchOffset+i] & 0xFFFF)) {
                return false;
            }            
        }
        return true;
    }

    private final boolean isMatch(VmAddress start, int offset, short[] match,
            int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if ((Unsafe.getShort(start, offset + (i * 2)) & 0xFFFF) != (match[matchOffset+i] & 0xFFFF)) {
                return false;
            }            
        }
        return true;
    }

    private final boolean isMatch(VmAddress start, int offset, int[] match,
            int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if (Unsafe.getInt(start, offset + (i * 4)) != match[matchOffset+i]) {
                return false;
            }            
        }
        return true;
    }

    private final boolean isMatch(VmAddress start, int offset, long[] match,
            int matchOffset, int matchLength) {
        for (int i = 0; i < matchLength; i++) {
            if (Unsafe.getLong(start, offset + (i * 8)) != match[matchOffset+i]) {
                return false;
            }            
        }
        return true;
    }
}

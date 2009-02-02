/*
 * $Id$
 *
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
 
package org.jnode.awt.font.truetype;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Data input for true type files. All methods are named as the data formats in the true type
 * specification.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public abstract class TTFInput {

    private Stack<Long> filePosStack = new Stack<Long>();
    private int tempFlags;

    public abstract TTFInput createSubInput(int offset, int length) throws IOException;

    // --------------- IO ---------------

    public abstract void seek(long offset) throws IOException;

    abstract long getPointer() throws IOException;

    public void pushPos() throws IOException {
        filePosStack.push(new Long(getPointer()));
    }

    public void popPos() throws IOException {
        seek(((Long) filePosStack.pop()).longValue());
    }

    public void close() throws IOException {
        // Do nothing, overwrite where applicable
    }

    // ---------- Simple Data Types --------------

    public abstract int readRawByte() throws IOException;

    public abstract int readByte() throws IOException;

    public abstract short readShort() throws IOException;

    public abstract int readUShort() throws IOException;

    public abstract long readULong() throws IOException;

    public abstract int readLong() throws IOException;

    public abstract byte readChar() throws IOException;

    public final short readFWord() throws IOException {
        return readShort();
    }

    public final int readUFWord() throws IOException {
        return readUShort();
    }

    public final double readFixed() throws IOException {
        int major = readShort();
        int minor = readShort();
        return major + minor / 16384d;
    }

    public final double readF2Dot14() throws IOException {
        int major = readByte();
        int minor = readByte();
        int fraction = minor + ((major & 0x3f) << 8);
        int mantissa = major >> 6;
        if (mantissa >= 2)
            mantissa -= 4;
        return mantissa + fraction / 16384d;
    }

    // ------------------------------------------------------------

    public final void checkShortZero() throws IOException {
        if (readShort() != 0) {
            System.err.println("Reserved bit should be 0.");
        }
    }

    public static final boolean checkZeroBit(int b, int bit, String name) throws IOException {
        if (flagBit(b, bit)) {
            System.err.println("Reserved bit " + bit + " in " + name + " not 0.");
            return false;
        } else {
            return true;
        }
    }

    // ---------------- Flags --------------------

    /**
     * Reads unsigned short flags into a temporary variable which can be queried using the flagBit
     * method. *
     *
     * @throws IOException
     */
    public void readUShortFlags() throws IOException {
        tempFlags = readUShort();
    }

    /**
     * Reads byte flags into a temporary variable which can be queried using the flagBit method. *
     *
     * @throws IOException
     */
    public void readByteFlags() throws IOException {
        tempFlags = readByte();
    }

    public boolean flagBit(int bit) {
        return flagBit(tempFlags, bit);
    }

    public static boolean flagBit(int b, int bit) {
        return (b & (1 << bit)) > 0;
    }

    // ---------------- Arrays -------------------

    public abstract void readFully(byte[] b) throws IOException;

    public int[] readFFFFTerminatedUShortArray() throws IOException {
        final List<Integer> values = new LinkedList<Integer>();
        int ushort = -1;
        do {
            ushort = readUShort();
            values.add(new Integer(ushort));
        } while (ushort != 0xFFFF);
        int[] shorts = new int[values.size()];
        int j = 0;
        for (int v : values) {
            shorts[j++] = v;
        }
        return shorts;
    }

    public int[] readUShortArray(int n) throws IOException {
        int[] temp = new int[n];
        for (int i = 0; i < temp.length; i++)
            temp[i] = readUShort();
        return temp;
    }

    public short[] readShortArray(int n) throws IOException {
        short[] temp = new short[n];
        for (int i = 0; i < temp.length; i++)
            temp[i] = readShort();
        return temp;
    }

}

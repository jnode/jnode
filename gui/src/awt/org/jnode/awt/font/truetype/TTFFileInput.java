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

package org.jnode.awt.font.truetype;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Concrete implementation of the TrueType Input for one Table, read from a TTF File.
 * <p/>
 * Reads one table from the file.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public class TTFFileInput extends TTFInput {

    private RandomAccessFile ttf;
    private long offset, length;

    public TTFFileInput(RandomAccessFile file, long offset, long length) throws IOException {
        this.ttf = file;
        this.offset = offset;
        this.length = length;
        //this.checksum = checksum;
    }

    public TTFFileInput(RandomAccessFile file) throws IOException {
        this(file, 0, file.length());
    }

    public TTFInput createSubInput(int offset, int length)
        throws IOException {
        return new TTFFileInput(ttf, this.offset + offset, length);
    }

    // --------------- IO ---------------

    public void seek(long offset) throws IOException {
        ttf.seek(this.offset + offset);
        //System.out.println("seek "+(this.offset+offset));
    }

    long getPointer() throws IOException {
        return ttf.getFilePointer() - offset;
    }

    // ---------- Simple Data Types --------------

    public int readByte() throws IOException {
        return ttf.readUnsignedByte();
    }

    public int readRawByte() throws IOException {
        return ttf.readByte() & 255;
    }

    public short readShort() throws IOException {
        return ttf.readShort();
    }

    public int readUShort() throws IOException {
        return ttf.readUnsignedShort();
    }

    public int readLong() throws IOException {
        return ttf.readInt();
    }

    public long readULong() throws IOException {
        byte[] temp = new byte[4];
        ttf.readFully(temp);
        long l = 0;
        long weight = 1;
        for (int i = 0; i < temp.length; i++) {
            //l |= (temp[3-i]&255) << (8*i);
            l += (temp[3 - i] & 255) * weight;
            weight *= 256;
        }
        return l;
    }

    public byte readChar() throws IOException {
        return ttf.readByte();
    }

    // ---------------- Arrays -------------------

    public void readFully(byte[] b) throws IOException {
        ttf.readFully(b);
    }

    public String toString() {
        return offset + "-" + (offset + length - 1);
    }

    /**
     * @throws IOException
     * @see org.jnode.awt.font.truetype.TTFInput#close()
     */
    public void close()
        throws IOException {
        super.close();
        ttf.close();
    }

}

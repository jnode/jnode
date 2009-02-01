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
 
package org.jnode.fs.jfat;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author gvt
 */
public class FatDirEntry {
    public static final int LENGTH = 32;

    protected static final int EOD = 0x00;
    protected static final int FREE = 0xE5;
    protected static final int KANJI = 0x05;

    protected static final int NO_INDEX = -1;

    protected final FatFileSystem fs;
    protected final FatMarshal entry;
    protected int index;

    private boolean lastDirEntry = false;
    private boolean freeDirEntry = false;

    protected FatDirEntry(FatFileSystem fs, FatMarshal entry, int index) {
        this.fs = fs;
        this.entry = entry;
        this.index = index;
    }

    private FatDirEntry(FatFileSystem fs, FatMarshal entry, int index, int flag) {
        this(fs, entry, index);

        if (flag == FREE)
            this.freeDirEntry = true;
        else
            this.lastDirEntry = true;
    }

    /*
     * FatDirEntry factory from a FatMarshal buffer
     */
    public static FatDirEntry create(FatFileSystem fs, FatMarshal entry, int index)
        throws IOException {
        int flag;
        FatAttr attr;

        flag = entry.getUInt8(0);
        attr = new FatAttr(entry.getUInt8(11));

        switch (flag) {
            case FREE:
            case EOD:
                return new FatDirEntry(fs, entry, index, flag);

            default:
                if (attr.isLong())
                    return new FatLongDirEntry(fs, entry, index);
                else
                    return new FatShortDirEntry(fs, entry, index);
        }
    }

    public void delete() {
        entry.setUInt8(0, FREE);
    }

    public FatFileSystem getFatFileSystem() {
        return fs;
    }

    public int getIndex() {
        return index;
    }

    protected void setIndex(int value) {
        if (value < 0)
            throw new IllegalArgumentException("value<0");
        index = value;
    }

    public int length() {
        return entry.length();
    }

    public ByteBuffer getByteBuffer() {
        return entry.getByteBuffer();
    }

    public boolean isLastDirEntry() {
        return lastDirEntry;
    }

    public boolean isFreeDirEntry() {
        return freeDirEntry;
    }

    public boolean isLongDirEntry() {
        return false;
    }

    public boolean isShortDirEntry() {
        return false;
    }

    public boolean isDirty() {
        return entry.isDirty();
    }

    public void flush() {
        entry.flush();
    }

    public String toString() {
        StrWriter out = new StrWriter();
        if (isFreeDirEntry()) {
            out.println("*******************************************");
            out.println("Free  Entry ");
            out.println("*******************************************");
            out.println("Index\t\t" + getIndex());
            out.print("*******************************************");
        } else if (isLastDirEntry()) {
            out.println("*******************************************");
            out.println("Last  Entry ");
            out.println("*******************************************");
            out.println("Index\t\t" + getIndex());
            out.print("*******************************************");
        } else {
            out.print(entry);
        }
        return out.toString();
    }
}

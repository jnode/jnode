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

import java.nio.ByteBuffer;


/**
 * @author gvt
 */
public class FatDirEntry {
    public static final int LENGTH = 32;

    public static final int EOD = 0x00;
    public static final int FREE = 0xE5;
    public static final int INVALID = 0xFF;
    public static final int KANJI = 0x05;

    public static final int NO_INDEX = -1;

    protected final FatFileSystem fs;
    protected final FatMarshal entry;
    protected final int index;

    private boolean lastDirEntry = false;
    private boolean freeDirEntry = false;

    public FatDirEntry(FatFileSystem fs, FatMarshal entry, int index) {
        this.fs = fs;
        this.entry = entry;
        this.index = index;
    }

    public FatDirEntry(FatFileSystem fs, FatMarshal entry, int index, int flag) {
        this(fs, entry, index);

        if (flag == FREE)
            this.freeDirEntry = true;
        else
            this.lastDirEntry = true;
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

    public void setFreeDirEntry(boolean freeDirEntry) {
        this.freeDirEntry = freeDirEntry;
    }

    public FatMarshal getEntry() {
        return entry;
    }

    @Override
    public String toString() {
        return String.format("FatDirEntry [%s] index:%d", entry, index);
    }

    public String toDebugString() {
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

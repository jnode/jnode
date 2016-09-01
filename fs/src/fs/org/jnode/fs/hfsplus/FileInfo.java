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
 
package org.jnode.fs.hfsplus;

import org.jnode.util.BigEndian;

public class FileInfo {
    private int fileType;
    private int fileCreator;
    private int finderFlags;

    public FileInfo(byte[] data, int offset) {
        fileType = BigEndian.getInt32(data, offset);
        fileCreator = BigEndian.getInt32(data, offset + 4);
        finderFlags = BigEndian.getUInt16(data, offset + 8);
    }

    public int getFileType() {
        return fileType;
    }

    public int getFileCreator() {
        return fileCreator;
    }

    public int getFinderFlags() {
        return finderFlags;
    }
}

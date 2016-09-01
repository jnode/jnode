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

public class HfsPlusBSDInfo {

    /**
     * The flag indicating not to dump the file (UF_NODUMP).
     */
    public static final int USER_FLAG_NO_DUMP = 0x1;

    /**
     * The flag indicating that the file cannot be changed (UF_IMMUTABLE).
     */
    public static final int USER_FLAG_IMMUTABLE = 0x2;

    /**
     * The flag indicating that the file can only be appended to (UF_APPEND).
     */
    public static final int USER_FLAG_APPEND_ONLY = 0x4;

    /**
     * The flag indicating that the directory is opaque (UF_OPAQUE).
     */
    public static final int USER_FLAG_OPAQUE = 0x8;

    /**
     * The flag indicating that the file may not be removed or renamed (UF_NOUNLINK).
     */
    public static final int USER_FLAG_NO_UNLINK = 0x10;

    /**
     * The flag indicating that the file is compressed (UF_COMPRESSED).
     */
    public static final int USER_FLAG_COMPRESSED = 0x20;

    /**
     * A flag hinting to the GUI that the item should not be displayed (UF_HIDDEN).
     */
    public static final int USER_FLAG_HIDDEN = 0x8000;

    private long ownerID;
    private long groupID;
    private int adminFlags;
    private int ownerFlags;
    private int fileMode;
    private long special;

    public HfsPlusBSDInfo(byte[] data, int offset) {
        ownerID = BigEndian.getUInt32(data, offset);
        groupID = BigEndian.getUInt32(data, offset + 4);
        adminFlags = BigEndian.getUInt8(data, offset + 8);
        ownerFlags = BigEndian.getUInt8(data, offset + 9);
        fileMode = BigEndian.getUInt16(data, offset + 10);
        special = BigEndian.getUInt32(data, offset + 12);
    }

    public long getOwnerID() {
        return ownerID;
    }

    public long getGroupID() {
        return groupID;
    }

    public int getAdminFlags() {
        return adminFlags;
    }

    public int getOwnerFlags() {
        return ownerFlags;
    }

    public int getFileMode() {
        return fileMode;
    }

    public long getSpecial() {
        return special;
    }
}

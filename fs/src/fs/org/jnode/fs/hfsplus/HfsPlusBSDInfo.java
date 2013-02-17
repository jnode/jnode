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
 
package org.jnode.fs.hfsplus;

import org.jnode.util.BigEndian;

public class HfsPlusBSDInfo {
    private int ownerID;
    private int groupID;
    private int adminFlags;
    private int ownerFlags;
    private int fileMode;
    private int special;

    public HfsPlusBSDInfo(byte[] data, int offset) {
        ownerID = BigEndian.getInt32(data, offset);
        groupID = BigEndian.getInt32(data, offset + 4);
        adminFlags = data[offset + 8];
        ownerFlags = data[offset + 9];
        fileMode = BigEndian.getInt16(data, offset + 10);
        special = BigEndian.getInt32(data, offset + 12);
    }

    public int getOwnerID() {
        return ownerID;
    }

    public int getGroupID() {
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

    public int getSpecial() {
        return special;
    }
}

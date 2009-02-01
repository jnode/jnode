/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.fs.hfsplus;

public class HfsPlusConstants {
    public static final int HFSPLUS_SUPER_MAGIC = 0x482b;

    public static final int HFSPLUS_MIN_VERSION = 0x0004; /* HFS+ */
    public static final int HFSPLUS_CURRENT_VERSION = 5; /* HFSX */

    /* HFS+ volume attributes */
    public static final int HFSPLUS_VOL_UNMNT_BIT = 8;
    public static final int HFSPLUS_VOL_SPARE_BLK_BIT = 9;
    public static final int HFSPLUS_VOL_NOCACHE_BIT = 10;
    public static final int HFSPLUS_VOL_INCNSTNT_BIT = 11;
    public static final int HFSPLUS_VOL_NODEID_REUSED_BIT = 12;
    public static final int HFSPLUS_VOL_JOURNALED_BIT = 13;
    public static final int HFSPLUS_VOL_SOFTLOCK_BIT = 15;

    public static final int BT_LEAF_NODE = -1;
    public static final int BT_INDEX_NODE = 0;
    public static final int BT_HEADER_NODE = 1;
    public static final int BT_MAP_NODE = 2;

    /* Types */
    public static final int RECORD_TYPE_FOLDER = 0x0001;
    public static final int RECORD_TYPE_FILE = 0x0002;
    public static final int RECORD_TYPE_FOLDER_THREAD = 0x0003;
    public static final int RECORD_TYPE_FILE_THREAD = 0x0004;

    public static final int kJIJournalInFSMask = 0x00000001;
    public static final int kJIJournalOnOtherDeviceMask = 0x00000002;
    public static final int kJIJournalNeedInitMask = 0x00000004;

    public static final byte EK_DATA_FORK = (byte) 0x00;
    public static final byte EK_RESOURCE_FORK = (byte) 0xFF;

    
    
}

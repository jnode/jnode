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
 
package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSUnicodeString;
import org.jnode.util.BigEndian;



public class CatalogThread {
    
    public static final int CATALOG_THREAD_SIZE = 512;
    /**The catalog thread record type. Can be a file or a folder. */
    private int recordType;
    /** the catalog node id of the file or folder referenced by the thread record. */
    private CatalogNodeId parentId;
    /** the name of the file or folder reference by the thread record. */
    private HFSUnicodeString nodeName;
    
    /**
     * Create catalog thread from existing data.
     * 
     * @param src byte array contains catalog thread data.
     */
    public CatalogThread(final byte[] src) {
        byte[] data = new byte[512];
        System.arraycopy(src, 0, data, 0, CATALOG_THREAD_SIZE);
        recordType = BigEndian.getInt16(data, 0);
        parentId = new CatalogNodeId(data, 4);
        nodeName = new HFSUnicodeString(data, 8);
    }

    /**
     * Create a new catalog thread.
     * 
     * @param type  catalog thread record type.
     * @param parent {@link CatalogNodeId} of the file or folder reference by the tread record.
     * @param name {@link HFSUnicodeString} represent the name of the file or folder reference by the tread record.
     */
    public CatalogThread(int type, CatalogNodeId parent, HFSUnicodeString name) {
       this.recordType = type;
       this.parentId = parent;
       this.nodeName = name;
    }
    
    /**
     * 
     * @return
     */
    public byte[] getBytes() {
    	byte[] data = new byte[512];
        BigEndian.setInt16(data, 0, recordType);
        BigEndian.setInt32(data, 4, parentId.getId());
        System.arraycopy(parentId.getBytes(), 0, data, 4, 4);
        System.arraycopy(nodeName.getBytes(), 0, data, 8, nodeName.getBytes().length);
        return data;
    }
    
}

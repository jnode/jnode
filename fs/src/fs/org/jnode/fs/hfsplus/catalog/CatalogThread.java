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
    
    private byte[] data;

    public CatalogThread(final byte[] src) {
        data = new byte[512];
        System.arraycopy(src, 0, data, 0, CATALOG_THREAD_SIZE);
    }

    /**
     * Create a new catalog thread.
     * 
     * @param type
     * @param parent
     * @param name
     */
    public CatalogThread(int type, CatalogNodeId parent, HFSUnicodeString name) {
        data = new byte[512];
        BigEndian.setInt16(data, 0, type);
        BigEndian.setInt32(data, 4, parent.getId());
        System.arraycopy(parent.getBytes(), 0, data, 4, 4);
        System.arraycopy(name.getBytes(), 0, data, 8, name.getBytes().length);
    }

    public final int getRecordType() {
        return BigEndian.getInt16(data, 0);
    }

    public final CatalogNodeId getParentId() {
        return new CatalogNodeId(data, 4);
    }

    public final HFSUnicodeString getNodeName() {
        return new HFSUnicodeString(data, 8);
    }
    
    public byte[] getBytes() {
        return data;
    }
}

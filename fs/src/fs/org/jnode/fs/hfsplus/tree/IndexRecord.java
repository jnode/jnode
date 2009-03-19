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
 
package org.jnode.fs.hfsplus.tree;

import org.jnode.util.BigEndian;

public class IndexRecord extends AbstractNodeRecord {
    /** A node number that represent a child node of the index node. */
    private int index;

    /**
     * 
     * @param key
     * @param nodeData
     * @param offset
     */
    public IndexRecord(final Key key, final byte[] nodeData, final int offset) {
        this.key = key;
        this.recordData = new byte[4];
        System.arraycopy(nodeData, offset + key.getKeyLength(), recordData, 0, 4);
        index = BigEndian.getInt32(recordData, 0);
    }
   
    public final int getIndex() {
        return index;
    }
    
}

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
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

public class CatalogKey extends AbstractKey {
    
    public static final int MINIMUM_KEY_LENGTH = 6;
    public static final int MAXIMUM_KEY_LENGTH = 516;

    private HFSUnicodeString nodeName;

    /**
     * 
     * @param src
     * @param offset
     */
    public CatalogKey(final byte[] src, final int offset) {
        int currentOffset = offset;
        byte[] ck = new byte[2];
        System.arraycopy(src, currentOffset, ck, 0, 2);
        keyLength = BigEndian.getInt16(ck, 0);
        currentOffset += 2;
        ck = new byte[4];
        System.arraycopy(src, currentOffset, ck, 0, 4);
        parentID = new CatalogNodeId(ck, 0);
        currentOffset += 4;
        if (keyLength > MINIMUM_KEY_LENGTH) {
            nodeName = new HFSUnicodeString(src, currentOffset);
        }
    }

    /**
     * 
     * @param parentID
     * @param name
     */
    public CatalogKey(final CatalogNodeId parentID, final HFSUnicodeString name) {
        this.parentID = parentID;
        this.nodeName = name;
        this.keyLength = MINIMUM_KEY_LENGTH + name.getLength();
    }

    public final int getKeyLength() {
        return keyLength;
    }

    public final CatalogNodeId getParentId() {
        return parentID;
    }

    public final HFSUnicodeString getNodeName() {
        return nodeName;
    }

    public final int compareTo(final Key o) {
        if (o instanceof CatalogKey) {
            CatalogKey ck = (CatalogKey) o;
            if (getParentId().getId() == ck.getParentId().getId()) {
                return nodeName.getUnicodeString().compareTo(
                        ck.getNodeName().getUnicodeString());
            } else if (getParentId().getId() < ck.getParentId().getId()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

    public byte[] getBytes() {
        byte[] data = new byte[this.getKeyLength()];
        BigEndian.setInt16(data, 0, this.getKeyLength());
        System.arraycopy(parentID.getBytes(), 0, data, 2, 4);
        System.arraycopy(nodeName.getBytes(), 0, data, 6, nodeName.getLength());
        return data;
    }
    
    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("[length, Parent ID, Node name]:").append(getKeyLength()).append(",").append(getParentId().getId())
                .append(",").append((getNodeName() != null) ? getNodeName().getUnicodeString() : "");
        return s.toString();
    }
    
}

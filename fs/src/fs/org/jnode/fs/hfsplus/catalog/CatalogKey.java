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
 
package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

/**
 * Implementation of catalog file key. The catalog file key is defined as following :
 * <ul>
 * <li>The length of the key</li>
 * <li>The node identifier of the parent folder</li>
 * <li>The name of the file or folder</li>
 * </ul>
 *
 * The minimal length for a key is 6 bytes. 2 bytes for the length and 4 bytes for the catalog node id.
 *
 */
public class CatalogKey extends AbstractKey {

    public static final int MINIMUM_KEY_LENGTH = 6;
    public static final int MAXIMUM_KEY_LENGTH = 516;
    /**
     * Catalog node id of the folder that contains file or folder represented by
     * the record. For thread records, contains the catalog node id of the file
     * or folder itself.
     */
    private CatalogNodeId parentId;
    /**
     * Name of the file or folder, empty for thread records.
     */
    private HfsUnicodeString nodeName;

    /**
     * Create catalog key from existing data.
     * 
     * @param src
     * @param offset
     */

    public CatalogKey(final byte[] src, final int offset) {
        int currentOffset = offset;
        byte[] ck = new byte[2];
        System.arraycopy(src, currentOffset, ck, 0, 2);
        //TODO Understand why the +2 is necessary
        keyLength = BigEndian.getUInt16(ck, 0) + 2;
        currentOffset += 2;
        ck = new byte[4];
        System.arraycopy(src, currentOffset, ck, 0, 4);
        parentId = new CatalogNodeId(ck, 0);
        currentOffset += 4;
        if (keyLength > MINIMUM_KEY_LENGTH) {
            nodeName = new HfsUnicodeString(src, currentOffset);
        }
    }

    /**
     * Create new catalog key based on parent CNID, ignoring file/folder name.
     *
     * @param parentID Parent catalog node identifier.
     */
    public CatalogKey(CatalogNodeId parentID) {
        this(parentID, new HfsUnicodeString(""));
    }

    /**
     * Create new catalog key based on parent CNID and the name of the file or
     * folder.
     * 
     * @param parentID Parent catalog node identifier.
     * @param name Name of the file or folder.
     * 
     */
    public CatalogKey(final CatalogNodeId parentID, final HfsUnicodeString name) {
        this.parentId = parentID;
        this.nodeName = name;
        this.keyLength = MINIMUM_KEY_LENGTH + (name.getLength() * 2) + 2;
    }

    public final CatalogNodeId getParentId() {
        return parentId;
    }

    public final HfsUnicodeString getNodeName() {
        return nodeName;
    }

    /**
     * Compare two catalog keys. These keys are compared by parent id and next
     * by node name.
     * 
     * @param key
     * 
     */
    public final int compareTo(final Key key) {
        int res = -1;
        if (key instanceof CatalogKey) {
            CatalogKey ck = (CatalogKey) key;
            res = this.getParentId().compareTo(ck.getParentId());
            if (res == 0) {
                // Note: this is unlikely to be correct. See TN1150 section "Unicode Subtleties" for details
                // For reading in data is should be safe since the B-Tree will be pre-sorted, but for adding new entries
                // it will cause the order to be wrong.
                res = this.getNodeName().getUnicodeString()
                    .compareTo(ck.getNodeName().getUnicodeString());
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        return 73 ^ parentId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CatalogKey)) {
            return false;
        }

        CatalogKey otherKey = (CatalogKey) obj;
        return parentId.getId() == otherKey.parentId.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.hfsplus.tree.AbstractKey#getBytes()
     */
    public byte[] getBytes() {
        int length = this.getKeyLength();
        byte[] data = new byte[length];
        BigEndian.setInt16(data, 0, length);
        System.arraycopy(parentId.getBytes(), 0, data, 2, 4);
        System.arraycopy(nodeName.getBytes(), 0, data, 6, (nodeName.getLength() * 2) + 2);
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("[length, Parent ID, Node name]:").append(getKeyLength()).append(",")
            .append(getParentId().getId()).append(",")
            .append((getNodeName() != null) ? getNodeName().getUnicodeString() : "");
        return s.toString();
    }

}

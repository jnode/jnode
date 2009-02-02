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
 
package org.jnode.fs.ntfs;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexRoot extends NTFSStructure {

    public static final int SIZE = 0x10;

    /**
     * Initialize this instance.
     * @param attr
     */
    public IndexRoot(IndexRootAttribute attr) {
        super(attr, attr.getAttributeOffset());
    }

    /**
     * Gets the attribute type.
     * @return
     */
    public int getAttributeType() {
        return getUInt32AsInt(0x00);
    }

    /**
     * Gets the collation rule.
     * @return
     */
    public int getCollationRule() {
        return getUInt32AsInt(0x04);
    }

    /**
     * Size of each index block in bytes (in the index allocation attribute).
     * @return
     */
    public int getIndexBlockSize() {
        return getUInt32AsInt(0x08);
    }

    /**
     * Gets the number of clusters per index record.
     * @return
     */
    public int getClustersPerIndexBlock() {
        final int v = getInt8(0x0C);
        if (v < 0) {
            return 1;
        } else {
            return v;
        }
    }
}

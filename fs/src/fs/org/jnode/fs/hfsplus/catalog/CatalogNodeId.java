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

import org.jnode.util.BigEndian;

public class CatalogNodeId implements Comparable {
    private byte[] cnid;

    public CatalogNodeId(final byte[] src, final int offset) {
        cnid = new byte[4];
        System.arraycopy(src, offset, cnid, 0, 4);
    }

    public CatalogNodeId(final int nodeId) {
        cnid = new byte[4];
        BigEndian.setInt32(cnid, 0, nodeId);
    }

    /* Parent Of the Root */
    public static final CatalogNodeId HFSPLUS_POR_CNID = new CatalogNodeId(1);
    /* ROOT directory */
    public static final CatalogNodeId HFSPLUS_ROOT_CNID = new CatalogNodeId(2);
    /* EXTents B-tree */
    public static final CatalogNodeId HFSPLUS_EXT_CNID = new CatalogNodeId(3);
    /* CATalog B-tree */
    public static final CatalogNodeId HFSPLUS_CAT_CNID = new CatalogNodeId(4);
    /* BAD blocks file */
    public static final CatalogNodeId HFSPLUS_BAD_CNID = new CatalogNodeId(5);
    /* ALLOCation file */
    public static final CatalogNodeId HFSPLUS_ALLOC_CNID = new CatalogNodeId(6);
    /* STARTup file */
    public static final CatalogNodeId HFSPLUS_START_CNID = new CatalogNodeId(7);
    /* ATTRibutes file */
    public static final CatalogNodeId HFSPLUS_ATTR_CNID = new CatalogNodeId(8);
    /* ExchangeFiles temp id */
    public static final CatalogNodeId HFSPLUS_EXCH_CNID = new CatalogNodeId(15);
    /* first available user id */
    public static final CatalogNodeId HFSPLUS_FIRSTUSER_CNID = new CatalogNodeId(16);

    public final int getId() {
        return BigEndian.getInt32(cnid, 0);
    }

    public final byte[] getBytes() {
        return cnid;
    }

    @Override
    public int compareTo(Object o) {
        Integer currentId = Integer.valueOf(this.getId());
        Integer compareId = Integer.valueOf(((CatalogNodeId) o).getId());
        return currentId.compareTo(compareId);
    }
}

/*
 * $Id$
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
 
package org.jnode.fs.ntfs;

import java.util.Iterator;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexRootAttribute extends NTFSResidentAttribute {

    private IndexRoot root;

    private IndexHeader header;

    /**
     * @param fileRecord
     * @param offset
     */
    public IndexRootAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Gets the index root structure.
     * 
     * @return
     */
    public IndexRoot getRoot() {
        if (root == null) {
            root = new IndexRoot(this);
        }
        return root;
    }

    /**
     * Gets the index header structure.
     * 
     * @return
     */
    public IndexHeader getHeader() {
        if (header == null) {
            header = new IndexHeader(this);
        }
        return header;
    }

    /**
     * Gets an iterator to iterate over all IndexEntry's in this index_root
     * attribute.
     * 
     * @return
     */
    public Iterator<IndexEntry> iterator() {
        final int headerOffset = getAttributeOffset() + IndexRoot.SIZE;
        return new IndexEntryIterator(getFileRecord(), this, headerOffset +
                getHeader().getFirstEntryOffset());
    }
}

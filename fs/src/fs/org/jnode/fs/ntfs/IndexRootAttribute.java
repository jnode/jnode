/*
 * $Id$
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
    public Iterator iterator() {
        final int headerOffset = getAttributeOffset() + IndexRoot.SIZE;
        return new IndexEntryIterator(getFileRecord(), this, headerOffset + getHeader()
                .getFirstEntryOffset());
    }
}
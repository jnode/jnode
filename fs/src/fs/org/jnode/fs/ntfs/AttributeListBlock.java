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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Data structure containing a list of {@link AttributeListEntry} entries.
 *
 * @author Daniel Noll (daniel@noll.id.au)
 */
final class AttributeListBlock extends NTFSStructure {

    /**
     * The length of the block.
     */
    private long length;

    /**
     * @param data binary data for the block.
     * @param offset the offset into the binary data.
     * @param length the length of the attribute list block, or 0 if unknown.
     */
    public AttributeListBlock(byte[] data, int offset, long length) {
        super(data, offset);
        this.length = length;
    }

    /**
     * Gets an iterator over all the entries in the attribute list.
     *
     * @return an iterator of all attribute list entries.
     */
    public Iterator<AttributeListEntry> getAllEntries() {
        return new AttributeListEntryIterator();
    }

    /**
     * Iteration of attribute list entries.
     */
    private class AttributeListEntryIterator implements Iterator<AttributeListEntry> {

        /**
         * The next element to return.
         */
        private AttributeListEntry nextElement;

        /**
         * Current offset being looked at.
         */
        private int offset = 0;

        /**
         * Returns {@code true} if there are more elements in the iteration.
         *
         * @return {@code true} if there are more elements in the iteration.
         */
        public boolean hasNext() {
            // Safety check in case hasNext is called twice without calling next.
            if (nextElement != null) {
                return true;
            }

            // If the length is specified, use it to determine where the block ends.
            if (offset + 4 > length) {
                return false;
            }
            
            int length = getUInt16(offset + 0x04);
            nextElement = new AttributeListEntry(AttributeListBlock.this, offset);
            offset += length;
            return true;
        }

        /**
         * Gets the next entry from the iteration.
         *
         * @return the next entry from the iteration.
         */
        public AttributeListEntry next() {
            if (hasNext()) {
                AttributeListEntry result = nextElement;
                nextElement = null;
                return result;
            } else {
                throw new NoSuchElementException("Iterator has no more entries");
            }
        }

        /**
         * @throws UnsupportedOperationException always.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

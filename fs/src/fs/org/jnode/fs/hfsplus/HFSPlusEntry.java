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
 
package org.jnode.fs.hfsplus;

import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSEntry;
import org.jnode.fs.spi.FSEntryTable;

public class HFSPlusEntry extends AbstractFSEntry {

    private LeafRecord record;

    public HFSPlusEntry(final HfsPlusFileSystem fs, final FSEntryTable table, final HFSPlusDirectory parent,
            final String name, final LeafRecord record) {
        super(fs, table, parent, name, getFSEntryType(name, record));
        this.record = record;
    }

    private static int getFSEntryType(final String name, final LeafRecord record) {
        int mode = record.getType();
        if ("/".equals(name)) {
            return AbstractFSEntry.ROOT_ENTRY;
        } else if (mode == HfsPlusConstants.RECORD_TYPE_FOLDER) {
            return AbstractFSEntry.DIR_ENTRY;
        } else if (mode == HfsPlusConstants.RECORD_TYPE_FILE) {
            return AbstractFSEntry.FILE_ENTRY;
        } else {
            return AbstractFSEntry.OTHER_ENTRY;
        }
    }

    public final LeafRecord getRecord() {
        return record;
    }
}

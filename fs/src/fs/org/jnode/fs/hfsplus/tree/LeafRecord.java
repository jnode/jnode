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

public class LeafRecord extends AbstractNodeRecord {

    public LeafRecord(final Key key, final byte[] recordData) {
        this.key = key;
        this.recordData = new byte[recordData.length];
        System.arraycopy(recordData, 0, this.recordData, 0, recordData.length);
    }

    public LeafRecord(final Key key, final byte[] nodeData, final int offset, final int recordDataSize) {
        this.key = key;
        this.recordData = new byte[recordDataSize];
        System.arraycopy(nodeData, offset + key.getKeyLength() + 2, this.recordData, 0, recordDataSize);
    }

    public final int getType() {
        return BigEndian.getInt16(this.recordData, 0);
    }

    public final String toString() {
        return "Type : " + getType() + "\nKey : " + getKey().toString() + "\n";
    }

}

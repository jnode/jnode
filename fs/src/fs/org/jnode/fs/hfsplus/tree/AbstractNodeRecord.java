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

public abstract class AbstractNodeRecord implements NodeRecord {

    protected Key key = null;
    protected byte[] recordData = null;

    public Key getKey() {
        return key;
    }

    public byte[] getData() {
        return recordData;
    }

    public int getSize() {
        return key.getKeyLength() + recordData.length;
    }

    public byte[] getBytes() {
        byte[] data = new byte[key.getKeyLength() + this.recordData.length];
        System.arraycopy(key.getBytes(), 0, data, 0, key.getKeyLength());
        System.arraycopy(this.recordData, 0, data, key.getKeyLength(), this.recordData.length);
        return data;
    }
}

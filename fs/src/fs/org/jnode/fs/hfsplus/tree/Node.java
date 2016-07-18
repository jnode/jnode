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
 
package org.jnode.fs.hfsplus.tree;

public interface Node<T extends NodeRecord> {

    public static final int OFFSET_SIZE = 2;

    public NodeDescriptor getNodeDescriptor();

    public int getRecordOffset(int index);

    public T getNodeRecord(int index);

    /**
     * Insert a record in the node.
     * 
     * @param record The record to insert.
     * @return True if record is correctly inserted, false if there is not
     *         enough place to insert the record.
     */
    public boolean addNodeRecord(T record);
}

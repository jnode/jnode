/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.tree.AbstractNode;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class CatalogLeafNode extends AbstractNode<LeafRecord> {

	private final Logger log = Logger.getLogger(getClass());

	/**
	 * Create a new node.
	 * 
	 * @param descriptor
	 * @param nodeSize
	 */
	public CatalogLeafNode(NodeDescriptor descriptor, final int nodeSize) {
		super(descriptor, nodeSize);
	}

	/**
	 * Create node from existing data.
	 * 
	 * @param nodeData
	 * @param nodeSize
	 */
	public CatalogLeafNode(final byte[] nodeData, final int nodeSize) {
		super(nodeData, nodeSize);

	}

	@Override
	protected void loadRecords(byte[] nodeData) {
		CatalogKey key;
		int offset;
		for (int i = 0; i < this.descriptor.getNumRecords(); i++) {
			offset = offsets.get(i);
			key = new CatalogKey(nodeData, offset);
			int recordSize = offsets.get(i + 1) - offset;
			records.add(new LeafRecord(key, nodeData, offset, recordSize));
		}
	}

	/**
	 * @param parentId
	 * @return a NodeRecord or {@code null}
	 */
	public final LeafRecord find(final CatalogNodeId parentId) {
		for (LeafRecord record : records) {
			log.debug("Record : " + record.toString() + " Parent ID : " + parentId.getId());
			CatalogKey key = (CatalogKey) record.getKey();
			if (key.getParentId().getId() == parentId.getId()) {
				return record;
			}
		}
		return null;
	}

	/**
	 * @param parentId
	 * @return an array of NodeRecords
	 */
	public final LeafRecord[] findAll(final CatalogNodeId parentId) {
		List<LeafRecord> list = new LinkedList<LeafRecord>();
		for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
			LeafRecord record = this.getNodeRecord(index);
			log.debug("Record : " + record.toString() + " Parent ID : " + parentId.getId());
			CatalogKey key = (CatalogKey) record.getKey();
			if (key.getParentId().getId() == parentId.getId()) {
				list.add(record);
			}
		}
		return list.toArray(new LeafRecord[list.size()]);
	}

}

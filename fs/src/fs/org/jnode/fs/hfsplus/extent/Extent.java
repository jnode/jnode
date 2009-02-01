/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.fs.hfsplus.tree.BTHeaderRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class Extent {
    private NodeDescriptor btnd;
    private BTHeaderRecord bthr;

    public Extent(HFSPlusParams params) {
        btnd = new NodeDescriptor();
        btnd.setKind(HfsPlusConstants.BT_HEADER_NODE);
        btnd.setHeight(0);
        btnd.setRecordCount(3);
        //
        bthr = new BTHeaderRecord();
        bthr.setTreeDepth(0);
        bthr.setRootNode(0);
        bthr.settFirstLeafNode(0);
        bthr.setLastLeafNode(0);
        bthr.setLeafRecords(0);
        bthr.setNodeSize(params.getExtentNodeSize());
        bthr.setTotalNodes(params.getExtentClumpSize()
                / params.getExtentNodeSize());
        bthr.setFreeNodes(bthr.getTotalNodes() - 1);
        bthr.setClumpSize(params.getExtentClumpSize());
        bthr.setMaxKeyLength(ExtentKey.KEY_LENGTH);
    }
}

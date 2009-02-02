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
 
package org.jnode.fs.hfsplus.extent;

import org.jnode.fs.hfsplus.tree.AbstractNode;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.fs.hfsplus.tree.NodeRecord;

public class ExtentNode extends AbstractNode {
    
    public ExtentNode(NodeDescriptor descriptor, final int nodeSize) {
        this.size = nodeSize;
        this.datas = new byte[nodeSize];
        System.arraycopy(descriptor.getBytes(), 0, datas, 0, NodeDescriptor.BT_NODE_DESCRIPTOR_LENGTH);
    }
    
    public ExtentNode(final byte[] nodeData, final int nodeSize) {
        this.size = nodeSize;
        this.datas = nodeData;
    }

    @Override
    public NodeRecord getNodeRecord(int index) {
        // TODO Auto-generated method stub
        return null;
    }

}

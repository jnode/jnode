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

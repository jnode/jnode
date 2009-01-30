package org.jnode.fs.hfsplus.tree;


public class LeafNode extends AbstractNode {

    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public LeafNode(final byte[] nodeData, final int nodeSize) {
        this.size = nodeSize;
        this.datas = nodeData;
    }
    
    /**
     * 
     * @param descriptor
     * @param nodeSize
     */
    public LeafNode(final NodeDescriptor descriptor, int nodeSize) {
        this.size = nodeSize;
    }
  
    @Override
    public NodeRecord getNodeRecord(int index) {
        return null;
    }

}

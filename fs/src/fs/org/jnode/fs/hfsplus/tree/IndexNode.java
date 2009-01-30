package org.jnode.fs.hfsplus.tree;

public class IndexNode extends AbstractNode {

    /**
     * 
     * @param descriptor
     * @param nodeData
     * @param nodeSize
     */
    public IndexNode(final byte[] nodeData, final int nodeSize) {
        this.size = nodeSize;
        this.datas = nodeData;
    }

    @Override
    public NodeRecord getNodeRecord(int index) {
        return null;
    }
}

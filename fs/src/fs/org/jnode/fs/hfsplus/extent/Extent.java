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

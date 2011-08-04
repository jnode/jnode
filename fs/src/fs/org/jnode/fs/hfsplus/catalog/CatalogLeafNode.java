package org.jnode.fs.hfsplus.catalog;

import java.util.LinkedList;
import java.util.List;

import org.jnode.fs.hfsplus.tree.AbstractNode;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;
import org.jnode.fs.hfsplus.tree.NodeRecord;

public class CatalogLeafNode extends AbstractNode<LeafRecord> {
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
            Key key = record.getKey();
            if (key instanceof CatalogKey) {
                if (((CatalogKey) key).getParentId().getId() == parentId.getId()) {
                    return record;
                }
            }
        }
        return null;
    }

    /**
     * @param parentId
     * @return an array of NodeRecords
     */
    public final LeafRecord[] findAll(final CatalogNodeId parentId) {
        List<NodeRecord> list = new LinkedList<NodeRecord>();
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            NodeRecord record = this.getNodeRecord(index);
            Key key = record.getKey();
            if (key instanceof CatalogKey &&
                    ((CatalogKey) key).getParentId().getId() == parentId.getId()) {
                list.add(record);
            }
        }
        return list.toArray(new LeafRecord[list.size()]);
    }

}

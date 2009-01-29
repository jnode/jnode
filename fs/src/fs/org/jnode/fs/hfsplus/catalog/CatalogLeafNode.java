package org.jnode.fs.hfsplus.catalog;

import java.util.LinkedList;
import java.util.List;

import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafNode;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class CatalogLeafNode extends LeafNode {

    public CatalogLeafNode(final NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }
    
    public CatalogLeafNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize) {
        super(descriptor, nodeData, nodeSize);
        for (int i = 0; i < records.length; ++i) {
            int currentOffset = getOffset(i);
            int recordDataSize = getOffset(i + 1) - currentOffset;
            Key key = new CatalogKey(nodeData, currentOffset);
            records[i] = new LeafRecord(key, nodeData, currentOffset, recordDataSize);
        }
    }
    
    
    
    /**
     * 
     * @param parentId
     * @return
     */
    public final LeafRecord find(final CatalogNodeId parentId) {
        for (LeafRecord rec : records) {
            Key key = rec.getKey();
            if (key instanceof CatalogKey) {
                if (((CatalogKey) key).getParentId().getId() == parentId.getId()) {
                    return rec;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param parentId
     * @return
     */
    public final LeafRecord[] findAll(final CatalogNodeId parentId) {
        List<LeafRecord> list = new LinkedList<LeafRecord>();
        for (LeafRecord rec : records) {
            Key key = rec.getKey();
            if (key instanceof CatalogKey && ((CatalogKey) key).getParentId().getId() == parentId.getId()) {
                list.add(rec);
            }
        }
        return list.toArray(new LeafRecord[list.size()]);
    }
    
    public byte[] getBytes() {
        return nodeData;
    }
}

package org.jnode.fs.hfsplus.catalog;

import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafNode;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class CatalogLeafNode extends LeafNode {
	
	private final Logger log = Logger.getLogger(getClass());
	
	public CatalogLeafNode(NodeDescriptor descriptor, byte[] nodeData, int nodeSize){
		super(descriptor, nodeData, nodeSize);
		for(int i = 0; i < records.length; ++i) {
		    int currentOffset = offsets[i];
		    Key key = new CatalogKey(nodeData, currentOffset);
		    records[i] = new LeafRecord(key ,nodeData, currentOffset);
		    log.debug("Record["+ i +"]:" + records[i].toString());
		}
	}
	
	public LeafRecord find(CatalogNodeId parentId){
		for(LeafRecord rec : records) {
			Key key = rec.getKey();
			if(key instanceof CatalogKey) {
				if(((CatalogKey)key).getParentId().getId() == parentId.getId())
					return rec;
			}
		}
		return null;
	}
}

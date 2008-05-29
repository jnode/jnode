package org.jnode.fs.hfsplus.catalog;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.tree.IndexNode;
import org.jnode.fs.hfsplus.tree.IndexRecord;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class CatalogIndexNode extends IndexNode {
	private final Logger log = Logger.getLogger(getClass());
	public CatalogIndexNode(final NodeDescriptor descriptor, final byte[] nodeData, final int nodeSize){
		super(descriptor, nodeData, nodeSize);
		for(int i = 0; i < records.length; ++i) {
		    int currentOffset = offsets[i];
		    Key currentKey = new CatalogKey(nodeData, currentOffset);
		    records[i] = new IndexRecord(currentKey, nodeData, currentOffset);
		    log.debug("Index record key:" + records[i].getKey().toString());
		}
	}
	/**
	 * 
	 * @param parentId
	 * @return
	 */
	public final IndexRecord find(final CatalogNodeId parentId){
		for(IndexRecord rec : records) {
			Key key = rec.getKey();
			if(key instanceof CatalogKey) {
				if(((CatalogKey)key).getParentId().getId() == parentId.getId()) {
					return rec;
				}
			}
		}
		return null;
	}
	
	public final IndexRecord[] findChilds(final CatalogNodeId parentId){
		LinkedList<IndexRecord> result = new LinkedList<IndexRecord>();
		IndexRecord largestMatchingRecord = null;
		CatalogKey largestMatchingKey = null;
		for(IndexRecord rec : records) {
			CatalogKey key = (CatalogKey)rec.getKey();
			if( key.getParentId().getId() < parentId.getId() && 
					(largestMatchingKey == null || key.compareTo(largestMatchingKey) > 0) ) {
				largestMatchingKey = key;
				largestMatchingRecord = rec;
			} else if(key.getParentId().getId() == parentId.getId()) {
				result.addLast(rec);
			}

		}

		if(largestMatchingKey != null) {
			result.addFirst(largestMatchingRecord);
		}
		return result.toArray(new IndexRecord[result.size()]);
	}
	
	public final IndexRecord find(final CatalogKey key){
		IndexRecord largestMatchingRecord = null;
		for(int i = 0; i < records.length; ++i) {
			if(records[i].getKey().compareTo(key) <= 0 && 
					(largestMatchingRecord == null || records[i].getKey().compareTo(largestMatchingRecord.getKey()) > 0)) {
				largestMatchingRecord = records[i];
			}
		}
		return largestMatchingRecord;
	}
}

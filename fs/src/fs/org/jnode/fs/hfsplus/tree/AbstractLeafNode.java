package org.jnode.fs.hfsplus.tree;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public abstract class AbstractLeafNode<K extends Key> extends AbstractNode<K, LeafRecord> {

    private static final Logger log = Logger.getLogger(AbstractLeafNode.class);

    /**
     * Create a new node.
     *
     * @param descriptor
     * @param nodeSize
     */
    public AbstractLeafNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData
     * @param nodeSize
     */
    public AbstractLeafNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);
    }

    @Override
    protected LeafRecord createRecord(Key key, byte[] nodeData, int offset, int recordSize) {
        return new LeafRecord(key, nodeData, offset, recordSize);
    }

    public final LeafRecord[] findAll(K key) {
        List<LeafRecord> list = new LinkedList<LeafRecord>();
        for (LeafRecord record : records) {
            log.debug("Record: " + record.toString() + " Key: " + key);
            K recordKey = (K) record.getKey();
            if (recordKey != null && recordKey.equals(key)) {
                list.add(record);
            }
        }
        return list.toArray(new LeafRecord[list.size()]);
    }
}


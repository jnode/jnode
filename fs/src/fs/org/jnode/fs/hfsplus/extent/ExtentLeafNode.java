package org.jnode.fs.hfsplus.extent;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.tree.AbstractLeafNode;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.hfsplus.tree.NodeDescriptor;

public class ExtentLeafNode extends AbstractLeafNode<ExtentKey> {

    private static final Logger log = Logger.getLogger(ExtentLeafNode.class);

    /**
     * Create a new node.
     *
     * @param descriptor
     * @param nodeSize
     */
    public ExtentLeafNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData
     * @param nodeSize
     */
    public ExtentLeafNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);

    }

    @Override
    protected ExtentKey createKey(byte[] nodeData, int offset) {
        return new ExtentKey(nodeData, offset);
    }

    @Override
    protected LeafRecord createRecord(Key key, byte[] nodeData, int offset, int recordSize) {
        return new LeafRecord(key, nodeData, offset, recordSize);
    }

    /**
     * Gets all overflow extents that match the given key.
     *
     * @param key the key to match.
     * @return the overflow extents.
     */
    public ExtentDescriptor[] getOverflowExtents(ExtentKey key) {
        List<ExtentDescriptor> overflowExtents = new LinkedList<ExtentDescriptor>();

        for (LeafRecord record : findAll(key)) {
            for (
                int offset = 0; offset < record.getData().length; offset += ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH) {
                ExtentDescriptor descriptor = new ExtentDescriptor(record.getData(), offset);
                overflowExtents.add(descriptor);
            }
        }

        return overflowExtents.toArray(new ExtentDescriptor[overflowExtents.size()]);
    }
}

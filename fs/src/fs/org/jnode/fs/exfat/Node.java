package org.jnode.fs.exfat;

import java.io.IOException;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public final class Node {

    public final static int ATTRIB_RO = 0x01;
    public final static int ATTRIB_HIDDEN = 0x02;
    public final static int ATTRIB_SYSTEM = 0x04;
    public final static int ATTRIB_VOLUME = 0x08;
    public final static int ATTRIB_DIR = 0x10;
    public final static int ATTRIB_ARCH = 0x20;

    public static Node createRoot(ExFatSuperBlock sb)
        throws IOException {

        final Node result = new Node(sb, sb.getRootDirCluster(), null);

        result.clusterCount = result.rootDirSize();
        result.flags = ATTRIB_DIR;

        return result;
    }

    public static Node create(
        ExFatSuperBlock sb, long startCluster, int flags,
        String name, boolean isContiguous, long size, EntryTimes times) {

        final Node result = new Node(sb, startCluster, times);

        result.name = name;
        result.isContiguous = isContiguous;
        result.size = size;
        result.flags = flags;

        return result;
    }

    private final ExFatSuperBlock sb;
    private final DeviceAccess da;
    private final long startCluster;
    private final EntryTimes times;

    private boolean isContiguous;
    private long clusterCount;
    private int flags;
    private String name;
    private long size;

    private Node(ExFatSuperBlock sb, long startCluster, EntryTimes times) {
        this.sb = sb;
        this.da = sb.getDeviceAccess();
        this.startCluster = startCluster;
        this.times = times;
    }

    /**
     * Gets the flags for this node.
     *
     * @return the flags.
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Returns whether this node is contiguous.
     *
     * @return {@code true} if contiguous.
     */
    public boolean isContiguous() {
        return isContiguous;
    }

    public boolean isDirectory() {
        return ((this.flags & ATTRIB_DIR) != 0);
    }

    public EntryTimes getTimes() {
        return times;
    }

    public ExFatSuperBlock getSuperBlock() {
        return sb;
    }

    public long getClusterCount() {
        return clusterCount;
    }

    public long getStartCluster() {
        return startCluster;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    /**
     * Determines the size of the root directory in clusters.
     *
     * @return the number of clusters for the root directoy
     * @throws IOException on read error
     */
    private long rootDirSize() throws IOException {
        long result = 0;
        long current = this.sb.getRootDirCluster();

        while (!Cluster.invalid(current)) {
            result++;
            current = nextCluster(current);
        }

        return result;
    }

    public long nextCluster(long cluster) throws IOException {
        Cluster.checkValid(cluster);

        if (this.isContiguous) {
            return cluster + 1;
        } else {
            final long fatOffset =
                sb.blockToOffset(this.sb.getFatBlockStart()) +
                    cluster * Cluster.SIZE;

            return this.da.getUint32(fatOffset);
        }
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append(Node.class.getName());
        result.append(" [name=");
        result.append(this.name);
        result.append(", contiguous=");
        result.append(this.isContiguous);
        result.append("]");

        return result.toString();
    }

}

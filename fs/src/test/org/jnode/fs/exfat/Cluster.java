
package org.jnode.fs.exfat;

import java.io.IOException;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class Cluster {

    /**
     * Marks a cluster containing a bad block.
     */
    private final static long BAD = 0xfffffff7l;

    /**
     * Marks the final cluster of a file or directory.
     */
    private final static long END = 0xffffffffl;

    /**
     * The first data cluster that can be used on exFAT file systems.
     */
    public final static long FIRST_DATA_CLUSTER = 2;

    /**
     * The size of an exFAT cluster in blocks.
     */
    public final static int SIZE = 4;

    public static boolean invalid(long cluster) {
        return ((cluster == END) || (cluster == BAD));
    }

    public static void checkValid(long cluster) throws IOException {
        if (cluster < FIRST_DATA_CLUSTER || invalid(cluster)) {
            throw new IOException("bad cluster number " + cluster);
        }
    }

    public static void checkValid(
        long cluster, ExFatSuperBlock sblk) throws IOException {

        checkValid(cluster);

        final long maxCluster = sblk.getClusterCount() + 1;

        if (cluster > maxCluster) {
            final StringBuilder sb = new StringBuilder();
            sb.append("cluster ").append(cluster);
            sb.append(" exceeds maximum of ").append(maxCluster);
            throw new IOException(sb.toString());
        }
    }

    private Cluster() {
        /* utility class, no instances */
    }

}

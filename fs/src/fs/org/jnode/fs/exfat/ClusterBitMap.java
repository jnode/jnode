/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.exfat;

import java.io.IOException;

/**
 * The exFAT free space bitmap.
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public final class ClusterBitMap {

    public static ClusterBitMap read(ExFatSuperBlock sb,
                                     long startCluster, long size) throws IOException {

        Cluster.checkValid(startCluster);

        final ClusterBitMap result = new ClusterBitMap(sb, startCluster, size);

        if (size < ((result.clusterCount + 7) / 8)) {
            throw new IOException("cluster bitmap too small");
        }

        return result;
    }

    /**
     * The super block of the file system holding this {@code ClusterBitMap}.
     */
    private final ExFatSuperBlock sb;

    /**
     * The first cluster of the {@code ClusterBitMap}.
     */
    private final long startCluster;

    /**
     * The size in bytes.
     */
    private final long size;

    private final long clusterCount;
    private final long devOffset;
    private final DeviceAccess da;

    private ClusterBitMap(
        ExFatSuperBlock sb, long startCluster, long size)
        throws IOException {

        this.sb = sb;
        this.da = sb.getDeviceAccess();
        this.startCluster = startCluster;
        this.size = size;
        this.clusterCount = sb.getClusterCount() - Cluster.FIRST_DATA_CLUSTER;
        this.devOffset = sb.clusterToOffset(startCluster);
    }

    public boolean isClusterFree(long cluster) throws IOException {
        Cluster.checkValid(cluster, this.sb);

        final long bitNum = cluster - Cluster.FIRST_DATA_CLUSTER;
        final long offset = bitNum / 8;
        final int bits = this.da.getUint8(offset + this.devOffset);
        return (bits & (1 << (bitNum % 8))) == 0;
    }

    /**
     * Gets the first cluster of the bitmap.
     *
     * @return the first cluster.
     */
    public long getStartCluster() {
        return startCluster;
    }

    /**
     * Gets the cluster count.
     *
     * @return the cluster count.
     */
    public long getClusterCount() {
        return clusterCount;
    }

    public long getUsedClusterCount() throws IOException {
        long result = 0;

        for (long i = 0; i < size; i++) {
            final int bits = this.da.getUint8(this.devOffset + i);
            result += Integer.bitCount(bits);
        }

        return result;
    }

}

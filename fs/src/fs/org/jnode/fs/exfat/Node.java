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
import org.apache.log4j.Logger;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public final class Node {

    public static final int ATTRIB_RO = 0x01;
    public static final int ATTRIB_HIDDEN = 0x02;
    public static final int ATTRIB_SYSTEM = 0x04;
    public static final int ATTRIB_VOLUME = 0x08;
    public static final int ATTRIB_DIR = 0x10;
    public static final int ATTRIB_ARCH = 0x20;

    public static Node createRoot(ExFatSuperBlock sb)
        throws IOException {

        final Node result = new Node(sb, sb.getRootDirCluster(), new EntryTimes(null, null, null));

        result.clusterCount = result.rootDirSize();
        result.flags = ATTRIB_DIR;

        return result;
    }

    public static Node create(
        ExFatSuperBlock sb, long startCluster, int flags,
        String name, boolean isContiguous, long size, long allocatedSize, EntryTimes times, boolean deleted) {

        final Node result = new Node(sb, startCluster, times);

        result.name = name;
        result.isContiguous = isContiguous;
        result.size = size;
        result.allocatedSize = allocatedSize;
        result.flags = flags;
        result.deleted = deleted;

        if (allocatedSize < size) {
            Logger.getLogger(Node.class).warn("Allocated size less than file size: " + result);
        }

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

    /**
     * The size of the file in bytes.
     */
    private long size;

    /**
     * The size allocated for the file in bytes. This may be larger than {@code size} if the OS has reserved some space
     * for the file to grow into.
     */
    private long allocatedSize;
    private boolean deleted;

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

    public long getAllocatedSize() {
        return allocatedSize;
    }

    public boolean isDeleted() {
        return deleted;
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
        result.append(", size=");
        result.append(size);
        result.append(", allocated-size=");
        result.append(allocatedSize);
        result.append("]");

        return result.toString();
    }

}

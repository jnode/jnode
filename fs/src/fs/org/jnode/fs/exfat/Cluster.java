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
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class Cluster {

    /**
     * Marks a cluster containing a bad block.
     */
    private static final long BAD = 0xfffffff7l;

    /**
     * Marks the final cluster of a file or directory.
     */
    private static final long END = 0xffffffffl;

    /**
     * The first data cluster that can be used on exFAT file systems.
     */
    public static final long FIRST_DATA_CLUSTER = 2;

    /**
     * The size of an exFAT cluster in blocks.
     */
    public static final int SIZE = 4;

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

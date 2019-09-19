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

package org.jnode.fs.ntfs.datarun;

import java.io.IOException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.jnode.fs.ntfs.NTFSStructure;
import org.jnode.fs.ntfs.NTFSVolume;
import org.jnode.fs.util.FSUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class DataRun implements DataRunInterface {

    /**
     * logger
     */
    protected static final Logger log = Logger.getLogger(DataRun.class);

    /**
     * Cluster number of first cluster of this run. If this is zero, the run
     * isn't actually stored as it is all zero.
     */
    private final long cluster;

    /**
     * Length of datarun in clusters
     */
    private final long length;

    /**
     * Flag indicating that the data is not stored on disk but is all zero.
     */
    private boolean sparse = false;

    /**
     * Size in bytes of this datarun descriptor
     */
    private final int size;

    /**
     * First VCN of this datarun.
     */
    private long vcn;

    /**
     * Initialize this instance.
     *
     * @param cluster Cluster number of first cluster of this run.
     * @param length  Length of datarun in clusters
     * @param sparse  Flag indicating that the data is not stored on disk but is all zero.
     * @param size    Size in bytes of this datarun descriptor
     * @param vcn     First VCN of this datarun.
     */
    public DataRun(long cluster, long length, boolean sparse, int size, long vcn) {
        this.cluster = cluster;
        this.length = length;
        this.sparse = sparse;
        this.size = size;
        this.vcn = vcn;
    }

    /**
     * Initialize this instance.
     *
     * @param attr
     * @param offset
     * @param vcn         First VCN of this datarun.
     * @param previousLCN
     */
    public DataRun(NTFSStructure attr, int offset, long vcn, long previousLCN) {
        NTFSStructure dataRunStructure = new NTFSStructure(attr, offset);

        // read first byte in type attribute
        int type = dataRunStructure.getUInt8(0);
        final int lenlen = type & 0xF;
        final int clusterlen = type >>> 4;

        this.size = lenlen + clusterlen + 1;
        this.vcn = vcn;

        switch (lenlen) {
            case 0x00:
                length = 0;
                break;
            case 0x01:
                length = dataRunStructure.getUInt8(1);
                break;
            case 0x02:
                length = dataRunStructure.getUInt16(1);
                break;
            case 0x03:
                length = dataRunStructure.getUInt24(1);
                break;
            case 0x04:
                length = dataRunStructure.getUInt32(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid length length " + lenlen);
        }
        final long cluster;
        switch (clusterlen) {
            case 0x00:
                sparse = true;
                cluster = 0;
                break;
            case 0x01:
                cluster = dataRunStructure.getInt8(1 + lenlen);
                break;
            case 0x02:
                cluster = dataRunStructure.getInt16(1 + lenlen);
                break;
            case 0x03:
                cluster = dataRunStructure.getInt24(1 + lenlen);
                break;
            case 0x04:
                cluster = dataRunStructure.getInt32(1 + lenlen);
                break;
            case 0x05:
                cluster = dataRunStructure.getInt40(1 + lenlen);
                break;
            default:
                throw new IllegalArgumentException("Unknown cluster length " + clusterlen);
        }
        this.cluster = cluster == 0 ? 0 : cluster + previousLCN;
    }

    /**
     * Tests if this data run is a sparse run.  Sparse runs don't actually refer to
     * stored data, and are effectively a way to store a run of zeroes without storage
     * penalty.
     *
     * @return {@code true} if the run is sparse, {@code false} if it is not.
     */
    public boolean isSparse() {
        return sparse;
    }

    /**
     * @return Returns the cluster.
     */
    public long getCluster() {
        return this.cluster;
    }

    /**
     * Gets the size of this datarun descriptor in bytes.
     *
     * @return Returns the size.
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Gets the length of this datarun in clusters.
     *
     * @return Returns the length.
     */
    public long getLength() {
        return length;
    }

    /**
     * Gets the first VCN of this data run.
     *
     * @return Returns the vcn.
     */
    @Override
    public long getFirstVcn() {
        return this.vcn;
    }

    /**
     * Gets the last VCN of this data run.
     *
     * @return Returns the vcn.
     */
    @Override
    public long getLastVcn() {
        return getFirstVcn() + getLength() - 1;
    }

    /**
     * Read clusters from this datarun.
     *
     * @param vcn
     * @param dst
     * @param dstOffset
     * @param nrClusters
     * @param clusterSize
     * @param volume
     * @return The number of clusters read.
     * @throws IOException
     */
    public int readClusters(long vcn, byte[] dst, int dstOffset, int nrClusters, int clusterSize,
                            NTFSVolume volume) throws IOException {

        final long myFirstVcn = getFirstVcn();
        final long myLength = getLength();
        final long myLastVcn = getLastVcn();

        final long reqLastVcn = vcn + nrClusters - 1;

        if (log.isDebugEnabled()) {
            log.debug("me:" + myFirstVcn + "-" + myLastVcn + ", req:" + vcn + "-" + reqLastVcn);
        }

        if ((vcn > myLastVcn) || (myFirstVcn > reqLastVcn)) {
            // Not my region
            return 0;
        }

        final long actCluster; // Starting cluster
        final int count; // #clusters to read
        final int actDstOffset; // Actual dst offset
        if (vcn < myFirstVcn) {
            final int vcnDelta = (int) (myFirstVcn - vcn);
            count = FSUtils.checkedCast(Math.min(nrClusters - vcnDelta, myLength));
            actDstOffset = dstOffset + (vcnDelta * clusterSize);
            actCluster = getCluster();
        } else {
            // vcn >= myFirstVcn
            final int vcnDelta = (int) (vcn - myFirstVcn);
            count = FSUtils.checkedCast(Math.min(nrClusters, myLength - vcnDelta));
            actDstOffset = dstOffset;
            actCluster = getCluster() + vcnDelta;
        }

        if (log.isDebugEnabled()) {
            log.debug("cluster=" + cluster + ", length=" + length + ", dstOffset=" + dstOffset);
            log.debug("cnt=" + count + ", actclu=" + actCluster + ", actdstoff=" + actDstOffset);
        }

        // Zero the area
        Arrays.fill(dst, actDstOffset, actDstOffset + count * clusterSize, (byte) 0);

        if (!isSparse()) {
            volume.readClusters(actCluster, dst, actDstOffset, count);
        }

        return count;
    }

    /**
     * Maps a virtual cluster to a logical cluster.
     *
     * @param vcn the virtual cluster number to map.
     * @return the logical cluster number or -1 if this cluster is not stored (e.g. for a sparse cluster).
     * @throws ArrayIndexOutOfBoundsException if the VCN doesn't belong to this data run.
     */
    public long mapVcnToLcn(long vcn) {
        long myLastVcn = getFirstVcn() + getLength() - 1;

        if ((vcn > myLastVcn) || (getFirstVcn() > vcn)) {
            throw new ArrayIndexOutOfBoundsException("Invalid VCN for this data run: " + vcn);
        }

        long cluster = getCluster();

        if (cluster == 0 || isSparse()) {
            // This is a sparse cluster, not actually stored on disk
            return -1;
        }

        final int vcnDelta = (int) (vcn - getFirstVcn());
        return cluster + vcnDelta;
    }

    @Override
    public String toString() {
        return String.format("[%s-run vcn:%d-%d cluster:%d]", isSparse() ? "sparse" : "data", getFirstVcn(),
                             getLastVcn(), getCluster());
    }
}

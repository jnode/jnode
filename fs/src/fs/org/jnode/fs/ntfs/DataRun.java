/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DataRun extends NTFSStructure {

    /** Type of this datarun */
    private final int type;

    /**
     * Cluster number of first cluster of this run. If this is zero, the run
     * isn't actually stored as it is all zero.
     */
    private final long cluster;

    /** Length of datarun in clusters */
    private final int length;

    /** Size in bytes of this datarun descriptor */
    private final int size;

    /** First VCN of this datarun. */
    private long vcn;

    /**
     * Initialize this instance.
     * 
     * @param attr
     * @param offset
     * @param vcn First VCN of this datarun.
     * @param previousLCN
     */
    public DataRun(NTFSNonResidentAttribute attr, int offset, long vcn, long previousLCN) {
        super(attr, offset);
        // read first byte in type attribute
        this.type = getUInt8(0);
        final int lenlen = type & 0xF;
        final int clusterlen = type >>> 4;

        this.size = lenlen + clusterlen + 1;
        this.vcn = vcn;

        switch (lenlen) {
            case 0x00:
                length = 0;
                break;
            case 0x01:
                length = getUInt8(1);
                break;
            case 0x02:
                length = getUInt16(1);
                break;
            case 0x03:
                length = getUInt24(1);
                break;
            case 0x04:
                length = getUInt32AsInt(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid length length " + lenlen);
        }
        final int cluster;
        switch (clusterlen) {
            case 0x00:
                cluster = 0;
                break;
            case 0x01:
                cluster = getInt8(1 + lenlen);
                break;
            case 0x02:
                cluster = getInt16(1 + lenlen);
                break;
            case 0x03:
                cluster = getInt24(1 + lenlen);
                break;
            case 0x04:
                cluster = getInt32(1 + lenlen);
                break;
            default:
                throw new IllegalArgumentException("Unknown cluster length " + clusterlen);
        }
        this.cluster = cluster == 0 ? 0 : cluster + previousLCN;
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
    public int getLength() {
        return length;
    }

    /**
     * Gets the first VCN of this datarun.
     * 
     * @return Returns the vcn.
     */
    public final long getFirstVcn() {
        return this.vcn;
    }

    /**
     * Sets the first VCN of this datarun.
     * 
     * @param vcn the new VCN.
     */
    final void setFirstVcn(long vcn) {
        this.vcn = vcn;
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
        final int myLength = getLength();
        final long myLastVcn = myFirstVcn + myLength - 1;

        final long reqLastVcn = vcn + nrClusters - 1;

        log.debug("me:" + myFirstVcn + "-" + myLastVcn + ", req:" + vcn + "-" + reqLastVcn);

        if ((vcn > myLastVcn) || (myFirstVcn > reqLastVcn)) {
            // Not my region
            return 0;
        }

        final long actCluster; // Starting cluster
        final int count; // #clusters to read
        final int actDstOffset; // Actual dst offset
        if (vcn < myFirstVcn) {
            final int vcnDelta = (int) (myFirstVcn - vcn);
            count = Math.min(nrClusters - vcnDelta, myLength);
            actDstOffset = dstOffset + (vcnDelta * clusterSize);
            actCluster = getCluster();
        } else {
            // vcn >= myFirstVcn
            final int vcnDelta = (int) (vcn - myFirstVcn);
            count = Math.min(nrClusters, myLength - vcnDelta);
            actDstOffset = dstOffset;
            actCluster = getCluster() + vcnDelta;
        }

        log.debug("cluster=" + cluster + ", length=" + length + ", dstOffset=" + dstOffset);
        log.debug("cnt=" + count + ", actclu=" + actCluster + ", actdstoff=" + actDstOffset);

        if (actCluster == 0) {
            // Not really stored on disk -- sparse files, etc.
            Arrays.fill(dst, actDstOffset, actDstOffset + count * clusterSize, (byte) 0);
        } else {
            volume.readClusters(actCluster, dst, actDstOffset, count);
        }

        return count;
    }
}

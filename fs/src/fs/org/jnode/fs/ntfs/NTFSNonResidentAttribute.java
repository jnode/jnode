/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.util.ArrayList;
import java.util.List;

/**
 * An NTFS file attribute that has its data stored outside the attribute.
 * The attribute itself contains a runlist refering to the actual data.
 *  
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Daniel Noll (daniel@noll.id.au) (compression support)
 */
public class NTFSNonResidentAttribute extends NTFSAttribute {

    private int numberOfVCNs = 0;

    private final List<DataRunInterface> dataRuns = new ArrayList<DataRunInterface>();

    /**
     * @param fileRecord
     * @param offset
     */
    public NTFSNonResidentAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
        /*
         * process the dataruns...all non resident attributes have their data
         * outside. can find where using data runs
         */
        final int dataRunsOffset = getDataRunsOffset();
        if (dataRunsOffset > 0) {
            readDataRuns(dataRunsOffset);
        }
    }

    /**
     * @see org.jnode.fs.ntfs.NTFSAttribute#processAttributeData(byte[])
     */
    /*
     * public void processAttributeData(byte[] buffer) { // TODO Auto-generated
     * method stub }
     */

    /**
     * @return Returns the startVCN.
     */
    public long getStartVCN() {
        return getUInt32(0x10);
    }

    public long getLastVCN() {
        return getUInt32(0x18);
    }

    /**
     * @return Returns the dataRunsOffset.
     */
    public int getDataRunsOffset() {
        return getUInt16(0x20);
    }

    /**
     * Gets the compression unit size.  2 to the power of this value is the number of clusters
     * per compression unit.
     *
     * @return the compression unit size. 
     */
    public int getCompressionUnitSize() {
        return getUInt16(0x22);
    }

    /**
     * Gets the size allocated to the attribute.  May be larger than the actual size of the
     * attribute data.
     *
     * @return the size allocated to the attribute.
     */
    public long getAttributeAllocatedSize() {
        return getUInt32(0x28);
    }

    /**
     * Gets the actual size taken up by the attribute data.
     *
     * @return the actual size taken up by the attribute data.
     */
    public long getAttributeActualSize() {
        return getUInt32(0x30);
    }

    /**
     * Read the dataruns. It is called only for non resident attributes.
     */
    private void readDataRuns(int parentoffset) {
        int offset = parentoffset;

        long previousLCN = 0;
        final List<DataRunInterface> dataruns = getDataRuns();
        long vcn = 0;

        // If this attribute is compressed we will coalesce compressed/sparse
        // data run pairs into a single data run object for convenience when reading.
        boolean compressed = (getFlags() & 0x0001) != 0;
        boolean expectingSparseRunNext = false;
        int compUnitSize = 1 << getCompressionUnitSize();

        while (getUInt8(offset) != 0x0) {
            final DataRun dataRun = new DataRun(this, offset, vcn, previousLCN);

            if (compressed) {
                if (dataRun.isSparse() && expectingSparseRunNext) {
                    // This is the sparse run which follows a compressed run.
                    // The number of runs it contains does not count towards the total
                    // as the compressed run reports holding all the runs for the pair.
                    // But we do need to move the offsets.  Leaving this block open in case
                    // later it makes sense to put some logic in here.
                } else if (dataRun.getLength() == compUnitSize) {
                    // Compressed/sparse pairs always add to the compression unit size.  If
                    // the unit only compresses to 16, the system will store it uncompressed.
                    // So this whole unit is stored as-is, we'll leave it as a normal data run.
                    dataruns.add(dataRun);
                    this.numberOfVCNs += dataRun.getLength();
                    vcn += dataRun.getLength();
                    previousLCN = dataRun.getCluster();
                } else {
                    // TODO: Is it possible for the length to be GREATER than the unit size?
                    dataruns.add(new CompressedDataRun(dataRun, compUnitSize));
                    if (dataRun.getLength() != compUnitSize) {
                        expectingSparseRunNext = true;
                    }

                    this.numberOfVCNs += compUnitSize;
                    vcn += compUnitSize;
                    previousLCN = dataRun.getCluster();
                }
            } else {
                // map VCN-> datarun
                dataruns.add(dataRun);
                this.numberOfVCNs += dataRun.getLength();
                vcn += dataRun.getLength();
                previousLCN = dataRun.getCluster();
            }

            offset += dataRun.getSize();
        }

        // check the dataruns
        final int clusterSize = getFileRecord().getVolume().getClusterSize();
        // Rounds up but won't work for 0, which shouldn't occur here.
        final long allocatedVCNs = (getAttributeAllocatedSize() - 1) / clusterSize + 1;
        if (this.numberOfVCNs != allocatedVCNs) {
            // Probably not a problem, often multiple attributes make up one allocation.
            log.debug("VCN mismatch between data runs and allocated size, possibly a composite attribute. " +
                      "data run VCNs = " + this.numberOfVCNs + ", allocated size = " + allocatedVCNs +
                      ", data run count = " + dataRuns.size());
        }
    }

    /**
     * @return Returns the data runs.
     */
    private List<DataRunInterface> getDataRuns() {
        return dataRuns;
    }

    /**
     * Read a number of clusters starting from a given virtual cluster number
     * (vcn).
     * 
     * @param vcn
     * @param nrClusters
     * @return The number of clusters read.
     * @throws IOException
     */
    public int readVCN(long vcn, byte[] dst, int dstOffset, int nrClusters) throws IOException {
        final int flags = getFlags();
        if ((flags & 0x4000) != 0) {
            throw new IOException("Reading encrypted files is not supported");
        }

        if (log.isDebugEnabled()) {
            log.debug("readVCN: wants start " + vcn + " length " + nrClusters +
                      ", we have start " + getStartVCN() + " length " + getNumberOfVCNs());
        }

        final NTFSVolume volume = getFileRecord().getVolume();
        final int clusterSize = volume.getClusterSize();
        int readClusters = 0;
        for (DataRunInterface dataRun : this.getDataRuns()) {
            readClusters += dataRun.readClusters(vcn, dst, dstOffset, nrClusters, clusterSize, volume);
            if (readClusters == nrClusters) {
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("readVCN: read " + readClusters);
        }

        return readClusters;
    }

    /**
     * @return Returns the numberOfVNCs.
     */
    public int getNumberOfVCNs() {
        return numberOfVCNs;
    }
}

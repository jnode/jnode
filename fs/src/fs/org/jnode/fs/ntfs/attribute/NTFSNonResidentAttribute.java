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
 
package org.jnode.fs.ntfs.attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.ntfs.CompressedDataRun;
import org.jnode.fs.ntfs.DataRun;
import org.jnode.fs.ntfs.DataRunInterface;
import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSVolume;
import org.jnode.fs.util.FSUtils;

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
     * Creates a new non-resident attribute and reads in the associated data runs.
     *
     * @param fileRecord the file record that owns this attribute.
     * @param offset the offset to read from.
     * @param fallbackCompressionUnit the fallback compression unit to use if the attribute is compressed but doesn't
     *   have a compression unit stored.
     */
    public NTFSNonResidentAttribute(FileRecord fileRecord, int offset, int fallbackCompressionUnit) {
        super(fileRecord, offset);
        /*
         * process the dataruns...all non resident attributes have their data
         * outside. can find where using data runs
         */
        final int dataRunsOffset = getDataRunsOffset();
        if (dataRunsOffset > 0) {
            readDataRuns(dataRunsOffset, fallbackCompressionUnit);
        }
    }

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
    public int getStoredCompressionUnitSize() {
        return getUInt16(0x22);
    }

    private int getCompressionUnitSize(int fallbackCompressionUnit) {
        int compressionUnitSize = getStoredCompressionUnitSize();

        if (compressionUnitSize == 0) {
            // It seems like in some situations the compression unit size is only stored onto the first attribute of a
            // certain type in the list. For example if there are three compressed DATA attributes, the second and third
            // attributes may not have this set. In that situation use the first attribute's compression unit size.
            return fallbackCompressionUnit;
        }

        return 1 << compressionUnitSize;
    }

    /**
     * Gets the size allocated to the attribute.  May be larger than the actual size of the
     * attribute data.
     *
     * @return the size allocated to the attribute.
     */
    public long getAttributeAllocatedSize() {
        return getInt64(0x28);
    }

    /**
     * Gets the actual size taken up by the attribute data.
     *
     * @return the actual size taken up by the attribute data.
     */
    public long getAttributeActualSize() {
        return getInt64(0x30);
    }

    /**
     * Gets the size that has been initialized by the attribute data. It is possible ot non-resident attribute to
     * allocate data runs that it hasn't yet used.
     *
     * @return the size that has been initialized by the attribute so far.
     */
    public long getAttributeInitializedSize() {
        return getInt64(0x38);
    }

    /**
     * Read the data runs.
     *
     * @param parentoffset
     * @param fallbackCompressionUnit the fallback compression unit to use if the attribute is compressed but doesn't
     *   have a compression unit stored.
     */
    private void readDataRuns(int parentoffset, int fallbackCompressionUnit) {
        int offset = parentoffset;

        long previousLCN = 0;
        final List<DataRunInterface> dataruns = getDataRuns();
        long vcn = 0;

        // If this attribute is compressed we will coalesce compressed/sparse
        // data run pairs into a single data run object for convenience when reading.
        boolean compressed = isCompressedAttribute();
        boolean expectingSparseRunNext = false;
        boolean firstDataRun = true;
        int lastCompressedSize = 0;
        int compUnitSize = compressed ? getCompressionUnitSize(fallbackCompressionUnit) : 1;

        while (getUInt8(offset) != 0x0) {
            final DataRun dataRun = new DataRun(this, offset, vcn, previousLCN);

            if (log.isDebugEnabled()) {
                log.debug("Data run at offset: " + offset  + " " + dataRun);
            }

            if (compressed) {
                if (dataRun.isSparse() && (expectingSparseRunNext || firstDataRun)) {
                    // This is the sparse run which follows a compressed run.
                    // The number of runs it contains does not count towards the total
                    // as the compressed run reports holding all the runs for the pair.
                    // But we do need to move the offsets.
                    expectingSparseRunNext = false;

                    // Also the sparse run following a compressed run can be coalesced with a subsequent 'real' sparse
                    // run. So add that in if we hit one
                    if (dataRun.getLength() + lastCompressedSize > compUnitSize) {
                        int length = dataRun.getLength() - (compUnitSize - lastCompressedSize);
                        dataruns.add(new DataRun(0, length, true, 0, vcn));

                        this.numberOfVCNs += length;
                        vcn += length;
                        lastCompressedSize = 0;
                    }
                } else if (dataRun.getLength() >= compUnitSize) {
                    // Compressed/sparse pairs always add to the compression unit size.  If
                    // the unit only compresses to 16, the system will store it uncompressed.
                    // Also if one-or more of these uncompressed runs happen next to each other then they can be
                    // coalesced into a single run and even coalesced into the next compressed run. In that case the
                    // compressed run needs to be split off

                    int remainder = dataRun.getLength() % compUnitSize;

                    if (remainder != 0) {
                        // Uncompressed run coalesced with compressed run. First add in the uncompressed portion:
                        int uncompressedLength = dataRun.getLength() - remainder;
                        DataRun uncompressed = new DataRun(dataRun.getCluster(), uncompressedLength, false, 0, vcn);
                        dataruns.add(uncompressed);
                        vcn += uncompressedLength;
                        this.numberOfVCNs += uncompressedLength;

                        // Next add in the compressed portion
                        DataRun compressedRun =
                            new DataRun(dataRun.getCluster() + uncompressedLength, remainder, false, 0, vcn);
                        dataruns.add(new CompressedDataRun(compressedRun, compUnitSize));
                        expectingSparseRunNext = true;
                        lastCompressedSize = remainder;

                        this.numberOfVCNs += compUnitSize;
                        vcn += compUnitSize;

                    } else {
                        dataruns.add(dataRun);
                        this.numberOfVCNs += dataRun.getLength();
                        vcn += dataRun.getLength();
                    }

                } else {
                    dataruns.add(new CompressedDataRun(dataRun, compUnitSize));
                    expectingSparseRunNext = true;
                    lastCompressedSize = dataRun.getLength();

                    this.numberOfVCNs += compUnitSize;
                    vcn += compUnitSize;
                }
            } else {
                // map VCN-> datarun
                dataruns.add(dataRun);
                this.numberOfVCNs += dataRun.getLength();
                vcn += dataRun.getLength();
                lastCompressedSize = 0;
                expectingSparseRunNext = false;
            }

            if (!dataRun.isSparse()) {
                previousLCN = dataRun.getCluster();
            }

            offset += dataRun.getSize();
            firstDataRun = false;
        }

        // check the dataruns
        final int clusterSize = getFileRecord().getClusterSize();
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
    public List<DataRunInterface> getDataRuns() {
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

    /**
     * Generates a hex dump of the attribute's data.
     *
     * @return the hex dump.
     */
    public String hexDump() {
        int length = getBuffer().length - getOffset();
        byte[] data = new byte[length];
        getData(0, data, 0, data.length);
        return FSUtils.toString(data);
    }

    @Override
    public String toString() {
        return String.format("[attribute (non-res) type=x%x name'%s' size=%d runs=%d]", getAttributeType(),
            getAttributeName(), getAttributeActualSize(), getDataRuns().size());
    }

    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder();

        try {
            for (DataRunInterface dataRun : getDataRuns()) {
                builder.append(dataRun);
                builder.append("\n");
            }
        } catch (Exception e) {
            builder.append("Error: " + e);
        }

        return String.format("%s\nData runs:\n%s\nData: %s", toString(), builder.toString(), hexDump());
    }
}

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
import java.util.List;
import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.NTFSVolume;
import org.jnode.fs.ntfs.datarun.DataRunDecoder;
import org.jnode.fs.ntfs.datarun.DataRunInterface;

/**
 * An NTFS file attribute that has its data stored outside the attribute.
 * The attribute itself contains a runlist refering to the actual data.
 *
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Daniel Noll (daniel@noll.id.au) (compression support)
 */
public class NTFSNonResidentAttribute extends NTFSAttribute {

    /**
     * The data run decoder.
     */
    private final DataRunDecoder dataRunDecoder;

    /**
     * Creates a new non-resident attribute and reads in the associated data runs.
     *
     * @param fileRecord the file record that owns this attribute.
     * @param offset the offset to read from.
     */
    public NTFSNonResidentAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);

        int compressionUnit = getCompressionUnitSize();
        dataRunDecoder = new DataRunDecoder(isCompressedAttribute(), compressionUnit);
    }

    /**
     * Gets the data run decoder.
     *
     * @return the decoder.
     */
    public DataRunDecoder getDataRunDecoder() {
        return dataRunDecoder;
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

    private int getCompressionUnitSize() {
        return 1 << getStoredCompressionUnitSize();
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
     * Gets the decoded data runs for this attribute.
     *
     * @return Returns the data runs.
     */
    public List<DataRunInterface> getDataRuns() {
        return dataRunDecoder.getDataRuns();
    }

    /**
     * Read a number of clusters starting from a given virtual cluster number
     * (vcn).
     *
     * @param vcn the virtual cluster to read.
     * @param nrClusters the number of clusters to read.
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
                ", we have start " + getStartVCN() + " length " + dataRunDecoder.getNumberOfVCNs());
        }

        final NTFSVolume volume = getFileRecord().getVolume();
        final int clusterSize = volume.getClusterSize();
        int readClusters = 0;
        for (DataRunInterface dataRun : getDataRuns()) {
            if (readClusters >= nrClusters) {
                break;
            }
            readClusters += dataRun.readClusters(vcn, dst, dstOffset, nrClusters, clusterSize, volume);
        }

        if (log.isDebugEnabled()) {
            log.debug("readVCN: read " + readClusters);
        }

        return readClusters;
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

/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An NTFS file attribute that has its data stored outside the attribute.
 * The attribute itself contains a runlist refering to the actual data.
 *  
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSNonResidentAttribute extends NTFSAttribute {

    private int numberOfVCNs = 0;

    private final List dataRuns = new ArrayList();

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
        final int flags = getFlags();
        if (flags > 0) {
            log.info("flags & 0x0001 = " + (flags & 0x0001));
        }

        final int dataRunsOffset = getDataRunsOffset();
        if (dataRunsOffset > 0) {
            readDataRuns(dataRunsOffset);
        }
    }

    /**
     * @see org.jnode.fs.ntfs.attributes.NTFSAttribute#processAttributeData(byte[])
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
     * @return Returns the attributeAlocatedSize.
     */
    public long getAttributeAlocatedSize() {
        return getUInt32(0x28);
    }

    /**
     * Read the dataruns. It is called only for non resident attributes.
     */
    private void readDataRuns(int parentoffset) {
        int offset = parentoffset;

        long previousLCN = 0;
        final List dataruns = getDataRuns();
        long vcn = 0;

        while (getUInt8(offset) != 0x0) {
            final DataRun dataRun = new DataRun(this, offset, vcn, previousLCN);
            // map VCN-> datarun
            dataruns.add(dataRun);
            this.numberOfVCNs += dataRun.getLength();
            offset += dataRun.getSize();
            previousLCN = dataRun.getCluster();
            vcn += dataRun.getLength();
        }
        // check the dataruns
        final int clusterSize = getFileRecord().getVolume().getClusterSize();
        if (this.numberOfVCNs != this.getAttributeAlocatedSize() / clusterSize) {
            log
                    .error("ERROR: The number of VCNs from the data runs is different than the allocated size!: - "
                            + this.numberOfVCNs);
            log.error("Alocatedsize = " + getAttributeAlocatedSize()
                    / clusterSize);
            log.error("number of data runs = " + dataRuns.size());
        }
    }

    /**
     * @return Returns the dataRuns.
     */
    public List getDataRuns() {
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
    public int readVCN(long vcn, byte[] dst, int dstOffset, int nrClusters)
            throws IOException {
        final NTFSVolume volume = getFileRecord().getVolume();
        final int clusterSize = volume.getClusterSize();

        int readClusters = 0;
        for (Iterator it = this.getDataRuns().iterator(); it.hasNext();) {
            final DataRun dataRun = (DataRun) it.next();
            readClusters += dataRun.readClusters(vcn, dst, dstOffset, nrClusters, clusterSize, volume);
            if (readClusters == nrClusters) {
                break;
            }
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

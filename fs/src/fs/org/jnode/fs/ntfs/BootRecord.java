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
 
package org.jnode.fs.ntfs;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class BootRecord extends NTFSStructure {

    private final String systemID;

    private final int bytesPerSector;

    private final int sectorsPerCluster;

    private final long mftLcn;

    private final int mediaDescriptor;

    private final int sectorsPerTrack;

    private final long totalSectors;

    /**
     * The volume serial number.
     */
    private final String serialNumber;

    /**
     * Size of a filerecord in bytes
     */
    private final int fileRecordSize;

    /**
     * Size of an index record in bytes
     */
    private final int indexRecordSize;

    /**
     * Size of a cluster in bytes
     */
    private final int clusterSize;

    /**
     * Initialize this instance.
     *
     * @param buffer
     */
    public BootRecord(byte[] buffer) {
        super(buffer, 0);
        this.systemID = new String(buffer, 0x03, 8);
        this.bytesPerSector = getUInt16(0x0B);
        this.sectorsPerCluster = getUInt8(0x0D);
        this.mediaDescriptor = getUInt8(0x15);
        this.sectorsPerTrack = getUInt16(0x18);
        this.totalSectors = getUInt32(0x28);
        this.mftLcn = getUInt32(0x30);
        final int clustersPerMFTRecord = getInt8(0x40);
        final int clustersPerIndexRecord = getInt8(0x44);
        this.serialNumber = String.format("%02X%02X-%02X%02X", buffer[0x4b], buffer[0x4a], buffer[0x49], buffer[0x48]);

        this.clusterSize = sectorsPerCluster * bytesPerSector;
        this.fileRecordSize = calcByteSize(clustersPerMFTRecord);
        this.indexRecordSize = calcByteSize(clustersPerIndexRecord);

        log.debug("FileRecordSize  = " + fileRecordSize);
        log.debug("IndexRecordSize = " + indexRecordSize);
        log.debug("TotalSectors    = " + totalSectors);
    }

    /**
     * @return Returns the bytesPerSector.
     */
    public int getBytesPerSector() {
        return this.bytesPerSector;
    }

    /**
     * @return Returns the mediaDescriptor.
     */
    public int getMediaDescriptor() {
        return this.mediaDescriptor;
    }

    /**
     * Gets the logical cluster number of the MFT.
     *
     * @return Returns the mFTPointer.
     */
    public long getMftLcn() {
        return mftLcn;
    }

    /**
     * @return Returns the sectorPerCluster.
     */
    public int getSectorsPerCluster() {
        return this.sectorsPerCluster;
    }

    /**
     * @return Returns the sectorsPerTrack.
     */
    public int getSectorsPerTrack() {
        return this.sectorsPerTrack;
    }

    /**
     * @return Returns the systemID.
     */
    public String getSystemID() {
        return this.systemID;
    }

    /**
     * Gets the system serial number.
     *
     * @return the serial number.
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * @return Returns the totalSectors.
     */
    public long getTotalSectors() {
        return this.totalSectors;
    }

    /**
     * Gets the size of a filerecord in bytes.
     *
     * @return
     */
    public int getFileRecordSize() {
        return fileRecordSize;
    }

    /**
     * Gets the size of a indexrecord in bytes.
     *
     * @return
     */
    public int getIndexRecordSize() {
        return indexRecordSize;
    }

    /**
     * Gets the size of a cluster bytes.
     *
     * @return
     */
    public int getClusterSize() {
        return clusterSize;
    }

    private final int calcByteSize(int clusters) {
        if (clusters > 0) {
            return clusters * clusterSize;
        } else {
            return (1 << -clusters);
        }
    }
}

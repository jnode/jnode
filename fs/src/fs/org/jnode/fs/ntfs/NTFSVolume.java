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

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Chira
 */
public class NTFSVolume {

    private static final Logger log = Logger.getLogger(NTFSVolume.class);

    public static final byte LONG_FILE_NAMES = 0x01;

    public static final byte DOS_8_3 = 0x02;

    private byte currentNameSpace = LONG_FILE_NAMES;

    private final BlockDeviceAPI api;

    // local chache for faster access
    private final int clusterSize;

    private final BootRecord bootRecord;

    private MasterFileTable mftFileRecord;

    private FileRecord rootDirectory;

    /**
     * Initialize this instance.  
     */
    public NTFSVolume(BlockDeviceAPI api) throws IOException {
        // I hope this is enaugh..should be
        this.api = api;

        // Read the boot sector
        final byte[] buffer = new byte[ 512];
        api.read(0, buffer, 0, 512);
        this.bootRecord = new BootRecord(buffer);
        this.clusterSize = bootRecord.getClusterSize();
    }

    /**
     * @return Returns the bootRecord.
     */
    final BootRecord getBootRecord() {
        return bootRecord;
    }

    /**
     * Read a single cluster.
     * 
     * @param cluster
     */
    public void readCluster(long cluster, byte[] dst, int dstOffset)
            throws IOException {
        final int clusterSize = getClusterSize();
        final long clusterOffset = cluster * clusterSize;

        log.debug("readCluster(" + cluster + ") " + (readClusterCount++));
        api.read(clusterOffset, dst, dstOffset, clusterSize);
    }

    private int readClusterCount;
    private int readClustersCount;
    
    /**
     * Read a number of clusters.
     * 
     * @param firstCluster
     * @param nrClusters
     *            The number of clusters to read.
     * @param dst
     *            Must have space for (nrClusters * getClusterSize())
     * @param dstOffset
     * @throws IOException
     */
    public void readClusters(long firstCluster, byte[] dst, int dstOffset,
            int nrClusters) throws IOException {
        log.debug("readClusters(" + firstCluster + ", " + nrClusters + ") " + (readClustersCount++));

        final int clusterSize = getClusterSize();

        final long clusterOffset = firstCluster * clusterSize;
        api.read(clusterOffset, dst, dstOffset, nrClusters * clusterSize);
    }

    /**
     * Gets the size of a cluster.
     * 
     * @return
     */
    public int getClusterSize() {
        return clusterSize;
    }

    /**
     * Gets the MFT.
     * @return Returns the mTFRecord.
     */
    public MasterFileTable getMFT() throws IOException {
        if (mftFileRecord == null) {
            final BootRecord bootRecord = getBootRecord();
            final int bytesPerFileRecord = bootRecord.getFileRecordSize();
            final int clusterSize = getClusterSize();

            final int nrClusters;
            if (bytesPerFileRecord < clusterSize) {
                nrClusters = 1;
            } else {
                nrClusters = bytesPerFileRecord / clusterSize;
            }
            final byte[] data = new byte[ nrClusters * clusterSize];
            readClusters(bootRecord.getMftLcn(), data, 0, nrClusters);
            mftFileRecord = new MasterFileTable(this, data, 0);
        }
        return mftFileRecord;

    }

    /**
     * Gets the root directory on this volume.
     * @return
     * @throws IOException
     */
    public FileRecord getRootDirectory() throws IOException {
        if (rootDirectory == null) {
            // Read the root directory
            final MasterFileTable mft = getMFT();
            
            rootDirectory = mft.getRecord(MasterFileTable.SystemFiles.ROOT);
            log.info("getRootDirectory: " + rootDirectory.getFileName());
        }
        return rootDirectory;
    }

    /**
     * @return Returns the currentNameSpace.
     */
    public byte getCurrentNameSpace() {
        return currentNameSpace;
    }
}

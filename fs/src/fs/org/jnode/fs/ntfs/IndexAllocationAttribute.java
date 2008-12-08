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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexAllocationAttribute extends NTFSNonResidentAttribute {

    /**
     * @param fileRecord
     * @param offset
     */
    public IndexAllocationAttribute(FileRecord fileRecord, int offset) {
        super(fileRecord, offset);
    }

    /**
     * Read an index block starting at a given vcn.
     * 
     * @param indexRoot
     * @param vcn
     * @return 
     * @throws IOException
     */
    public IndexBlock getIndexBlock(IndexRoot indexRoot, long vcn) throws IOException {
        log.debug("getIndexBlock(..," + vcn + ")");
        final FileRecord fileRecord = getFileRecord();

        // VCN passed in is relative to the size of index clusters, not filesystem clusters.
        // Calculate the actual offset we need in terms of filesystem clusters,
        // and how many actual clusters we will need to read.

        final int indexBlockSize = indexRoot.getIndexBlockSize();
        final int indexClusterSize = indexBlockSize / indexRoot.getClustersPerIndexBlock();
        final int fsClusterSize = fileRecord.getVolume().getClusterSize();
        final long fsVcn = vcn * indexClusterSize / fsClusterSize;
        final int fsNrClusters = (indexBlockSize - 1) / fsClusterSize + 1;
        final int offsetIntoVcn = (int) ((vcn * indexClusterSize) % fsClusterSize);

        final byte[] data = new byte[fsNrClusters * fsClusterSize];
        final int readClusters = readVCN(fsVcn, data, 0, fsNrClusters);
        if (readClusters != fsNrClusters) {
            // If we don't throw an error now, it just fails more mysteriously later!
            throw new IOException("Number of clusters read was not the number requested (requested " +
                                  fsNrClusters + ", read " + readClusters + ")");
        }

        return new IndexBlock(fileRecord, data, offsetIntoVcn);
    }
}

/*
 * $Id$
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
    public IndexAllocationAttribute(FileRecord fileRecord,
            int offset) {
        super(fileRecord, offset);
    }

    /**
     * Read an index block starting at a given vcn.
     * 
     * @param indexRoot
     * @param vcn
     * @return @throws
     *         IOException
     */
    public IndexBlock getIndexBlock(IndexRoot indexRoot, long vcn)
            throws IOException {
        log.debug("getIndexBlock(..," + vcn + ")");
        final FileRecord fileRecord = getFileRecord();
        final int nrClusters = indexRoot.getClustersPerIndexBlock();
        final byte[] data = new byte[ nrClusters
                * fileRecord.getVolume().getClusterSize()];
        readVCN(vcn, data, 0, nrClusters);
        return new IndexBlock(fileRecord, data, 0);
    }
}
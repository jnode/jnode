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

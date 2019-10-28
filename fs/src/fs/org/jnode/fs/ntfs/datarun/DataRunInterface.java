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
 
package org.jnode.fs.ntfs.datarun;

import java.io.IOException;
import org.jnode.fs.ntfs.NTFSVolume;

/**
 * @author Daniel Noll (daniel@noll.id.au)
 */
public interface DataRunInterface {

    /**
     * Gets the length of the data run in clusters.
     *
     * @return the length of the run in clusters.
     */
    long getLength();

    /**
     * Reads clusters from this datarun.
     *
     * @param vcn         the VCN to read, offset from the start of the entire file.
     * @param dst         destination buffer.
     * @param dstOffset   offset into destination buffer.
     * @param nrClusters  number of clusters to read.
     * @param clusterSize size of each cluster.
     * @param volume      reference to the NTFS volume structure.
     * @return the number of clusters read.
     * @throws IOException if an error occurs reading.
     */
    int readClusters(long vcn, byte[] dst, int dstOffset, int nrClusters, int clusterSize, NTFSVolume volume)
        throws IOException;

    /**
     * Gets the first virtual cluster in this data run.
     *
     * @return the first VCN.
     */
    long getFirstVcn();

    /**
     * Gets the last virtual cluster in this data run.
     *
     * @return the last VCN.
     */
    long getLastVcn();
}

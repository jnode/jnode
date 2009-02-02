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
 
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.system.BootLog;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ISO9660Volume implements ISO9660Constants {

    private final BlockDeviceAPI api;

    private final int blockSize;

    private final PrimaryVolumeDescriptor primaryVolumeDescriptor;

    private final SupplementaryVolumeDescriptor supplementaryVolumeDescriptor;

    /**
     * Initialize this instance.
     * 
     * @param api
     * @throws IOException
     */
    public ISO9660Volume(FSBlockDeviceAPI api) throws IOException {
        this.api = api;
        this.blockSize = api.getSectorSize();

        final byte[] buffer = new byte[blockSize];

        PrimaryVolumeDescriptor pVD = null;
        SupplementaryVolumeDescriptor sVD = null;
        boolean done = false;
        for (int currentLBN = 16; !done; currentLBN++) {
            // read the LB
            this.readFromLBN(currentLBN, 0, buffer, 0, blockSize);
            final int type = VolumeDescriptor.getType(buffer);
            switch (type) {
                case VolumeDescriptorType.TERMINATOR:
                    done = true;
                    break;
                case VolumeDescriptorType.BOOTRECORD:
                    BootLog.debug("Found boot record");
                    break;
                case VolumeDescriptorType.PRIMARY_DESCRIPTOR:
                    BootLog.debug("Found primary descriptor");
                    pVD = new PrimaryVolumeDescriptor(this, buffer);
                    // pVD.dump(System.out);
                    break;
                case VolumeDescriptorType.SUPPLEMENTARY_DESCRIPTOR:
                    BootLog.debug("Found supplementatory descriptor");
                    final SupplementaryVolumeDescriptor d =
                            new SupplementaryVolumeDescriptor(this, buffer);
                    if (d.isEncodingKnown()) {
                        sVD = d;
                    }
                    break;
                case VolumeDescriptorType.PARTITION_DESCRIPTOR:
                    BootLog.debug("Found partition descriptor");
                    break;
                default:
                    BootLog.debug("Found unknown descriptor with type " + type);
            }
        }
        if (pVD == null) {
            throw new IOException("No primary volume descriptor found");
        }
        this.primaryVolumeDescriptor = pVD;
        this.supplementaryVolumeDescriptor = sVD;
    }

    /**
     * Read a block of data from this volume.
     * 
     * @param startLBN
     * @param offset
     * @param buffer
     * @param bufferOffset
     * @param length
     * @throws IOException
     */
    final void readFromLBN(long startLBN, long offset, byte[] buffer, int bufferOffset, int length)
        throws IOException {
        api.read((startLBN * blockSize) + offset, ByteBuffer.wrap(buffer, bufferOffset, length));
    }

    /**
     * Gets the root directory entry of this volume.
     */
    public EntryRecord getRootDirectoryEntry() throws IOException {
        if (supplementaryVolumeDescriptor != null) {
            return supplementaryVolumeDescriptor.getRootDirectoryEntry();
        } else {
            return primaryVolumeDescriptor.getRootDirectoryEntry();
        }
    }
}

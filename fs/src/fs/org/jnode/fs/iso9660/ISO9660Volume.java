/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.system.BootLog;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ISO9660Volume {

    private final BlockDeviceAPI api;

    private final int blockSize;

    private final VolumeDescriptor primaryVolumeDescriptor;

    public ISO9660Volume(FSBlockDeviceAPI api) throws IOException {
        this.api = api;
        this.blockSize = api.getSectorSize();

        final byte[] buff = new byte[ blockSize];

        VolumeDescriptor pVD = null;
        boolean done = false;
        for (int currentLBN = 16; !done; currentLBN++) {
            // read the LB
            this.readFromLBN(currentLBN, 0, buff, 0, blockSize);
            final VolumeDescriptor vd = new VolumeDescriptor(this, buff);
            switch (vd.getType()) {
            case VolumeDescriptor.Type.TERMINATOR:
                done = true;
                break;
            case VolumeDescriptor.Type.BOOTRECORD:
                BootLog.info("Found boot record");
                break;
            case VolumeDescriptor.Type.PRIMARY_DESCRIPTOR:
                BootLog.info("Found primary descriptor");
                pVD = vd;
                break;
            case VolumeDescriptor.Type.SUPPLEMENTARY_DESCRIPTOR:
                BootLog.info("Found supplementatory descriptor");
                break;
            case VolumeDescriptor.Type.PARTITION_DESCRIPTOR:
                BootLog.info("Found partition descriptor");
                break;
            default:
                BootLog.info("Found unknown descriptor with type " + vd.getType());
            /*
             * if(vd.getType() ==
             * VolumeDescriptor.SupplementaryVolumeDescriptor_TYPE) {
             * this.setVolumeDescriptor(vd); }
             */
            }
        }
        if (pVD == null) { throw new IOException(
                "No primary volume descriptor found"); }
        this.primaryVolumeDescriptor = pVD;
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
    final void readFromLBN(long startLBN, long offset, byte[] buffer,
            int bufferOffset, int length) throws IOException {
        api.read((startLBN * blockSize) + offset, buffer, bufferOffset, length);
    }

    /**
     * @return Returns the volumeDescriptor.
     */
    public VolumeDescriptor getVolumeDescriptor() {
        return primaryVolumeDescriptor;
    }
}
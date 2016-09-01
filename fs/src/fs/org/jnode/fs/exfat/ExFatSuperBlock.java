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
 
package org.jnode.fs.exfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public final class ExFatSuperBlock extends AbstractFSObject {

    /**
     * The size of the ExFAT super block in bytes.
     */
    private static final int SIZE = 512;

    private static final String OEM_NAME = "EXFAT   "; //NOI18N

    private final DeviceAccess da;

    private long blockStart;
    private long blockCount;
    private long fatBlockStart;
    private long fatBlockCount;
    private long clusterBlockStart;
    private long clusterCount;
    private long rootDirCluster;
    private int volumeSerial;
    private byte fsVersionMinor;
    private byte fsVersionMajor;
    private short volumeState;
    private byte blockBits;
    private byte blocksPerClusterBits;
    private byte percentInUse;

    public ExFatSuperBlock(ExFatFileSystem fs) {
        super(fs);

        this.da = new DeviceAccess(fs.getApi());
    }

    public static ExFatSuperBlock read(ExFatFileSystem fs) throws IOException {

        final ByteBuffer b = ByteBuffer.allocate(SIZE);
        b.order(ByteOrder.LITTLE_ENDIAN);
        fs.getApi().read(0, b);
        
        /* check OEM name */

        final byte[] oemBytes = new byte[OEM_NAME.length()];
        b.position(0x03);
        b.get(oemBytes);
        final String oemString = new String(oemBytes);

        if (!OEM_NAME.equals(oemString)) {
            throw new IOException("OEM name mismatch");
        }

        /* check fat count */

        if ((b.get(0x6e) & 0xff) != 1) {
            throw new IOException("invalid FAT count");
        }

        /* check drive # */

        if ((b.get(0x6f) & 0xff) != 0x80) {
            throw new IOException("invalid drive number");
        }
        
        /* check boot signature */

        if ((b.get(510) & 0xff) != 0x55 || (b.get(511) & 0xff) != 0xaa)
            throw new IOException("missing boot sector signature");

        final ExFatSuperBlock result = new ExFatSuperBlock(fs);

        result.blockStart = b.getLong(0x40);
        result.blockCount = b.getLong(0x48);
        result.fatBlockStart = b.getInt(0x50);
        result.fatBlockCount = b.getInt(0x54);
        result.clusterBlockStart = b.getInt(0x58);
        result.clusterCount = b.getInt(0x5c);
        result.rootDirCluster = b.getInt(0x60);
        result.volumeSerial = b.getInt(0x64);
        result.fsVersionMinor = b.get(0x68);
        result.fsVersionMajor = b.get(0x69);
        result.volumeState = b.getShort(0x6a);
        result.blockBits = b.get(0x6c);
        result.blocksPerClusterBits = b.get(0x6d);
        result.percentInUse = b.get(0x70);

        /* check version */

        if (result.fsVersionMajor != 1) {
            throw new IOException("unsupported version major " +
                result.fsVersionMajor);
        }

        if (result.fsVersionMinor != 0) {
            throw new IOException("unsupported version minor " +
                result.fsVersionMinor);
        }

        return result;
    }

    public long clusterToBlock(long cluster) throws IOException {
        Cluster.checkValid(cluster);

        return this.clusterBlockStart +
            ((cluster - Cluster.FIRST_DATA_CLUSTER) <<
                this.blocksPerClusterBits);
    }

    public long blockToOffset(long block) {
        return (block << this.blockBits);
    }

    public long clusterToOffset(long cluster) throws IOException {
        return blockToOffset(clusterToBlock(cluster));
    }

    public void readCluster(ByteBuffer dest, long cluster) throws IOException {
        assert (dest.remaining() <= this.getBytesPerCluster())
            : "read over cluster bundary";

        da.read(dest, clusterToOffset(cluster));
    }

    public DeviceAccess getDeviceAccess() {
        return da;
    }

    public long getBlockStart() {
        return blockStart;
    }

    public long getBlockCount() {
        return blockCount;
    }

    public long getFatBlockStart() {
        return fatBlockStart;
    }

    public long getFatBlockCount() {
        return fatBlockCount;
    }

    public long getClusterBlockStart() {
        return clusterBlockStart;
    }

    /**
     * Returns the total number of data clusters available on the file system.
     * To iterate the clusters the range {@code 0..count} must be shifted by
     * {@link Cluster#FIRST_DATA_CLUSTER}.
     *
     * @return the number of usable clusters on the file system
     */
    public long getClusterCount() {
        return clusterCount;
    }

    public long getRootDirCluster() {
        return rootDirCluster;
    }

    public int getVolumeSerial() {
        return volumeSerial;
    }

    public byte getFsVersionMajor() {
        return fsVersionMajor;
    }

    public byte getFsVersionMinor() {
        return fsVersionMinor;
    }

    public short getVolumeState() {
        return volumeState;
    }

    public int getBlockSize() {
        return (1 << blockBits);
    }

    public int getBlocksPerCluster() {
        return (1 << blocksPerClusterBits);
    }

    public int getBytesPerCluster() {
        return (getBlockSize() << this.blocksPerClusterBits);
    }

    /**
     * Returns the percentage of allocated clusters, rounded down to
     * integer value. {@code 0xff} means this value is not available.
     *
     * @return the percent of used clusters
     */
    public byte getPercentInUse() {
        return percentInUse;
    }

}

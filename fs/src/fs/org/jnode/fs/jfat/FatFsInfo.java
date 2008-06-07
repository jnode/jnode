package org.jnode.fs.jfat;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.util.LittleEndian;

/**
 * @author tango
 */
public class FatFsInfo {

    private byte[] sector;

    public FatFsInfo(int size) {
        sector = new byte[size];
    }

    public void write(BlockDeviceAPI device, long offset) throws IOException {
        device.write(offset, ByteBuffer.wrap(sector));
    }

    protected int get8(int offset) {
        return LittleEndian.getUInt8(sector, offset);
    }

    protected void set8(int offset, int value) {
        LittleEndian.setInt8(sector, offset, value);
    }

    protected int get16(int offset) {
        return LittleEndian.getUInt16(sector, offset);
    }

    protected void set16(int offset, int value) {
        LittleEndian.setInt16(sector, offset, value);

    }

    protected long get32(int offset) {
        return LittleEndian.getUInt32(sector, offset);
    }

    protected void set32(int offset, long value) {
        LittleEndian.setInt32(sector, offset, (int) value);

    }

    protected String getString(int offset, int len) {
        StringBuilder b = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int v = sector[offset + i];
            b.append((char) v);
        }
        return b.toString();
    }

    protected void setString(int offset, int len, String value) {
        for (int i = 0; i < len; i++) {
            char ch;
            if (i < value.length())
                ch = value.charAt(i);
            else
                ch = (char) 0;
            LittleEndian.setInt8(sector, offset + i, ch);
        }

    }

    protected byte[] getBytes(int offset, int len) {
        byte[] v = new byte[len];

        System.arraycopy(sector, offset, v, 0, len);

        return v;
    }

    protected void setBytes(int offset, int len, byte[] value) {
        System.arraycopy(value, 0, sector, offset, len);

    }

    /**
     * The Lead signature that used to validate the signature that it s an FS_Info Sector.
     * @param FSI_LeadSig
     */
    public void setFsInfo_LeadSig(long FSI_LeadSig) {
        set32(0, FSI_LeadSig);
    }

    /**
     * This field is currently kept for the expansion.FAT32 Formatter code initialize it all to zero.Bytes in this 
     * field is not currently being used. 
     */
    public void setFsInfo_Reserved1() {
        byte[] reserve1 = new byte[480];
        setBytes(4, 480, reserve1);
    }

    /**
     * Another signature  that is more localized in the sector of the fields that are used. 
     * @param FSI_StrucSig
     */
    public void setFsInfo_StrucSig(int FSI_StrucSig) {
        set32(484, FSI_StrucSig);
    }

    /**
     * Contains the last known Free cluster count on the volume. If the value is 0xFFFFFFF,
     * then the Free count is unknown and must be Computed.Any other value can be used, but 
     * is not necessarily correct.It Should be range checked atleast make sure it is >=Volume Cluseter Count.
     * 
     * @param FSI_FreeCount
     */
    public void setFsInfo_FreeCount(int FSI_FreeCount) {
        set32(488, FSI_FreeCount);
    }

    public void setFsInfo_NextFree(int FSI_Nxt_Free) {
        set32(492, FSI_Nxt_Free);
    }

    public void setReserve2() {
        byte[] reserve2 = new byte[12];
        setBytes(496, 12, reserve2);
    }

    public void setFsInfo_TrailSig(int FSI_TrailSig) {
        set32(508, FSI_TrailSig);
    }

}

package org.jnode.driver.block.usb.storage;

import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.util.NumberUtils;

public class CBW extends USBPacket {

    public CBW() {
        super(31);
    }

    public void setSignature(int signature) {
        setInt(0, signature);
    }

    public void setTag(int tag) {
        setInt(4, tag);
    }

    public void setDataTransferLength(int dataTransferLength) {
        setInt(8, dataTransferLength);
    }

    public void setFlags(byte flags) {
        setByte(12, flags);
    }

    public void setLun(byte lun) {
        setByte(13, lun);
    }

    public void setLength(byte length) {
        setByte(14, (length & 0x07));
    }

    public void setCdb(byte[] cdb) {
        for (int offset = 0; offset < cdb.length; offset++) {
            setByte((offset + 15), cdb[offset]);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- CBW ---").append("\n");
        sb.append("Signature : 0x").append(NumberUtils.hex(getInt(0), 8)).append("\n");
        sb.append("Tag       : 0x").append(NumberUtils.hex(getInt(4), 8)).append("\n");
        sb.append("DTL       : 0x").append(NumberUtils.hex(getInt(8), 8)).append("\n");
        sb.append("Flag      : 0x").append(NumberUtils.hex(getByte(12), 2)).append("\n");
        sb.append("Lun       : 0x").append(NumberUtils.hex(getByte(13), 2)).append("\n");
        sb.append("Length    : 0x").append(NumberUtils.hex(getByte(14), 2)).append("\n");
        sb.append("Packet    : ").append(super.toString());
        return sb.toString();
    }
}

package org.jnode.driver.block.usb.storage;

import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.util.NumberUtils;

public class CSW extends USBPacket {

    public CSW() {
        super(13);
    }

    public void setSignature(int signature) {
        setInt(0, signature);
    }

    public void setFlag(int flag) {
        setInt(4, flag);
    }

    public void setResidue(int residue) {
        setInt(8, residue);
    }

    public void setStatus(byte status) {
        setByte(12, status);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- CSW ---").append("\n");
        sb.append("Signature : 0x").append(NumberUtils.hex(getInt(0), 8)).append("\n");
        sb.append("Flag      : 0x").append(NumberUtils.hex(getByte(4), 8)).append("\n");
        sb.append("Residue   : 0x").append(NumberUtils.hex(getByte(8), 8)).append("\n");
        sb.append("Status    : 0x").append(NumberUtils.hex(getByte(12), 2)).append("\n");
        sb.append("Packet    : ").append(super.toString());
        return sb.toString();
    }

}

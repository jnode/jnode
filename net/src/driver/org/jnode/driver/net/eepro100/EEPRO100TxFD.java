/*
 * Created on 21-Apr-2004
 *  
 */
package org.jnode.driver.net.eepro100;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.vm.VmAddress;

/**
 * @author flesire
 *  
 */
public class EEPRO100TxFD {
    private int TxFDSize = 16;
    private int DataBufferSize = 1536;
    /** The actual data */
    private final byte[] data;
    /** MemoryResource mapper around data */
    private final MemoryResource mem;
    /** Offset within mem of first DPD */
    private final int firstDPDOffset;
    /** Offset within mem of first ethernet frame */
    //private final int firstFrameOffset;
    /** 32-bit address first DPD */
    private final VmAddress firstDPDAddress;
    /** 32-bit address of first ethernet frame */
    //private final Address firstFrameAddress;
    /** */
    private int bufferAddress;

    /**
     *  
     */
    public EEPRO100TxFD(ResourceManager rm) {
        //      Create a large enough buffer
        final int size = (TxFDSize + DataBufferSize) + 16;
        this.data = new byte[size];
        this.mem = rm.asMemoryResource(data);

        final VmAddress memAddr = mem.getAddress();
        this.bufferAddress = VmAddress.as32bit(memAddr);
        int offset = 0;
        // Align on 16-byte boundary
        while ((bufferAddress & 15) != 0) {
            bufferAddress++;
            offset++;
        }

        this.firstDPDOffset = offset;
        this.firstDPDAddress = VmAddress.add(memAddr, firstDPDOffset);
        //this.firstFrameOffset = firstDPDOffset + TxFDSize;
        //this.firstFrameAddress = Address.add(memAddr, firstFrameOffset);
    }

    /**
     * Gets the address of the first DPD in this buffer.
     */
    public VmAddress getFirstDPDAddress() {
        return firstDPDAddress;
    }

    final int getStatus() {
        return mem.getInt(0);
    }

    final void setStatus(int value) {
        mem.setInt(0, value);
    }

    final int getLink() {
        return mem.getInt(4);
    }

    final void setLink(int value) {
        mem.setInt(4, value);
    }

    final void setDescriptorAddress(int value) {
        mem.setInt(8, value);
    }

    final int getCount() {
        return mem.getInt(12);
    }

    final void setCount(int value) {
        mem.setInt(12, value);
    }

    int getBufferAddress() {
        return this.bufferAddress;
    }

    void bufferAddress0(int address) {
        mem.setInt(16, address);
    }

    void bufferSize0(int size) {
        mem.setInt(20, size);
    }

    //  put paramater array into the cmd buffer
    void setParams(byte[] p) {
        for (int i = 0; i < p.length; i++) {
           mem.setShort(i + 8, p[i]);
        }
    }

}
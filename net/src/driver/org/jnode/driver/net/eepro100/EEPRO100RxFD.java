/*
 * Created on 21-Apr-2004
 *  
 */
package org.jnode.driver.net.eepro100;

import org.jnode.system.MemoryResource;

/**
 * @author flesire
 *  
 */
public class EEPRO100RxFD {

    private int bufferAddress;
    private MemoryResource mem;

    /**
     *  
     */
    public EEPRO100RxFD(MemoryResource mem) {
        this.mem = mem;
        /*
         * bufferAddress = frameAddress; packetOffset = 16;
         */
        mem.setInt(8, 0xffffffff);
    }

    final int getStatus() {
        return mem.getInt(0);
    }

    final void setStatus(int value) {
        mem.setInt(0, value);
    }

    final void setLink(int address) {
        mem.setInt(4, address);
    }

    final void setRxBufferAddress(int address) {
        mem.setInt(8, address);
    }

    final void setCount(int size) {
        mem.setInt(12, size);
    }

    final void flushHeader() {
        //SPROCKET_flush64(frameAddress);
        //SPROCKET_drainWriteBuffer();
    }

    final void cleanHeader() {
        /*
         * SPROCKET_clean64(frameAddress); SPROCKET_drainWriteBuffer();
         */
    }

    final int getCount() {
        return mem.getInt(12);
    }

    final String print() {
        return (Integer.toHexString(bufferAddress) + ": " 
                + Integer.toHexString(mem.getInt(4)) + ' ' 
                + Integer.toHexString(mem.getInt(8)) + ' ' 
                + Integer.toHexString(mem.getInt(12)));
    }

    /**
     * @return Returns the bufferAddress.
     */
    public int getBufferAddress() {
        return bufferAddress;
    }
}
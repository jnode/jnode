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
 
package org.jnode.driver.net.eepro100;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;

/**
 * This class provide access to Receive Descriptor.
 * 
 * @author flesire
 *  
 */
public class EEPRO100RxFD {

    private int RxFDSize = 16;
    private int DataBufferSize = 1518;

    private int bufferAddress;
    private byte[] data;
    private MemoryResource mem;

    /**
     *  
     */
    public EEPRO100RxFD(ResourceManager rm) {
        final int size = (RxFDSize + DataBufferSize) + 16;
		this.data = new byte[size];
		this.mem = rm.asMemoryResource(data);
		this.bufferAddress = mem.getAddress().toInt();
		setRxBufferAddress(0xffffffff);
    }

    public final int getStatus() {
        return mem.getInt(0);
    }

    public final void setStatus(int value) {
        mem.setInt(0, value);
    }
	
	public final int getCommand(){
		return mem.getInt(2);
	}
	
	public final void setCommand(int cmd){
		mem.setInt(2, cmd);
	}

	public final void setLink(int address) {
        mem.setInt(4, address);
    }
	
	public final void setRxBufferAddress(int address) {
        mem.setInt(8, address);
    }

	public final void setCount(int size) {
        mem.setInt(12, size);
    }

	public final void flushHeader() {
        //SPROCKET_flush64(frameAddress);
        //SPROCKET_drainWriteBuffer();
    }

	public final void cleanHeader() {
        /*
         * SPROCKET_clean64(frameAddress); SPROCKET_drainWriteBuffer();
         */
    }

	public final int getCount() {
        return mem.getInt(12);
    }

	public final String print() {
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
    
    /**
     * 
     * @return
     */
    public byte[] getDataBuffer() {
		byte[] buf = new byte[DataBufferSize];
		mem.getBytes(bufferAddress, buf, 0, buf.length);
		return buf;
	}
}

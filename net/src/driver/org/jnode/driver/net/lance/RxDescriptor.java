/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.net.SocketBuffer;
import org.jnode.system.MemoryResource;

/**
 * @author Chris Cole
 *
 */
public class RxDescriptor extends Descriptor {
	static final public int STATUS_FRAM = 0x2000;
	static final public int STATUS_OFLO = 0x1000;
	static final public int STATUS_CRC = 0x0800;
	static final public int STATUS_BUFF = 0x0400;

	public RxDescriptor(MemoryResource mem, int offset, int dataBufferOffset) {
		super(mem, offset, dataBufferOffset);

		setOwnerSelf(false);
	}
	
	public SocketBuffer getPacket() {
		//setOwnerSelf()
		return new SocketBuffer();
	}
	
	public void clearStatus() {
		mem.setShort(offset + STATUS, (short) STATUS_OWN);
	}
	
	public short getMessageByteCount() {
		return mem.getShort(offset + BCNT);
	}
	
	public byte[] getDataBuffer() {
		byte[] buf = new byte[getMessageByteCount()];
		mem.getBytes(dataBufferOffset, buf, 0, buf.length);
		return buf;
	}
}

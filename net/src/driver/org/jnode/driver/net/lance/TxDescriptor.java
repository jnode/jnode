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
public class TxDescriptor extends Descriptor {
	public TxDescriptor(MemoryResource mem, int offset, int dataBufferOffset) {
		super(mem, offset, dataBufferOffset);

		setOwnerSelf(true);
	}

	public void transmit(SocketBuffer skbuf) {
		// fill the data buffer
		mem.setBytes(skbuf.toByteArray(), 0, dataBufferOffset, skbuf.getSize());
		setStatus((short) (STATUS_OWN | STATUS_STP | STATUS_ENP));
		setByteCount(skbuf.getSize());
	}

	private void setByteCount(int bcnt) {
		mem.setShort(offset + 0x04, (short) (-bcnt));
	}
}
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
 
package org.jnode.driver.net.lance;

import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;

/**
 * @author Chris Cole
 * 
 */
public class Descriptor {
	static final public int MESSAGE_DESCRIPTOR_SIZE = 0x10;

	static final public int STATUS = 0x06;

	static final public int STATUS_OWN = 0x8000;

	static final public int STATUS_ERR = 0x4000;

	static final public int STATUS_STP = 0x0200;

	static final public int STATUS_ENP = 0x0100;

	static final public int STATUS_BPE = 0x0080;

	static final public int BCNT = 0x08;

	protected final MemoryResource mem;

	protected final int offset;

	protected final int dataBufferOffset;

	protected byte[] data;

	public Descriptor(MemoryResource mem, int offset, int dataBufferOffset) {

		this.mem = mem;
		this.offset = offset;
		this.dataBufferOffset = dataBufferOffset;

		data = new byte[BufferManager.DATA_BUFFER_SIZE];

		// final int buffAddress =
		// Address.as32bit(Address.addressOfArrayData(data));
		// Set the address
		final int buffAddress = mem.getAddress().add(dataBufferOffset).toInt();
		mem.setInt(offset + 0x00, buffAddress);
		mem.setShort(offset + 0x04, (short) (-BufferManager.DATA_BUFFER_SIZE));
		mem.setShort(offset + STATUS, (short) 0);
		mem.setInt(offset + 0x08, 0);
		mem.setInt(offset + 0x0C, 0);
	}

	public boolean isOwnerSelf() {
		return ((STATUS_OWN & mem.getShort(offset + STATUS)) == 0);
	}

	public void setOwnerSelf(boolean self) {
		if (self) {
			mem.setShort(offset + STATUS, (short) (0x7FFF & mem.getShort(offset
					+ STATUS)));
		} else {
			mem.setShort(offset + STATUS, (short) (STATUS_OWN | mem
					.getShort(offset + STATUS)));
		}
	}

	public short getStatus() {
		return mem.getShort(offset + STATUS);
	}

	public void setStatus(short status) {
		mem.setShort(offset + STATUS, status);
	}

	public void dumpData(Logger out) {
		for (int i = 0; i <= MESSAGE_DESCRIPTOR_SIZE - 1; i += 4) {
			out.debug("0x"
					+ NumberUtils.hex(mem.getAddress().toInt()
							+ offset + i) + " : 0x" + NumberUtils.hex((byte) i)
					+ " : 0x" + NumberUtils.hex(mem.getInt(offset + i)));
		}
	}
}

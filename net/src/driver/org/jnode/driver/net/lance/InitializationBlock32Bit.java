/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.apache.log4j.Logger;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;
import org.jnode.vm.VmAddress;

/**
 * @author Chris Cole
 *
 */
public class InitializationBlock32Bit {
	static public final int INIT_BLOCK_SIZE = 0x1C;

	private MemoryResource mem;
	private int offset;

	public InitializationBlock32Bit(
		MemoryResource mem,
		int offset,
		short mode,
		EthernetAddress physicalAddr,
		long logicalAddr,
		RxDescriptorRing rxRing,
		TxDescriptorRing txRing) {

		this.mem = mem;
		this.offset = offset;

		// Populate the initial data structure
		mem.setShort(offset + 0x00, mode);
		mem.setByte(offset + 0x02, getEncodedRingLength(rxRing.getLength()));
		mem.setByte(offset + 0x03, getEncodedRingLength(txRing.getLength()));
		mem.setByte(offset + 0x04, physicalAddr.get(0));
		mem.setByte(offset + 0x05, physicalAddr.get(1));
		mem.setByte(offset + 0x06, physicalAddr.get(2));
		mem.setByte(offset + 0x07, physicalAddr.get(3));
		mem.setByte(offset + 0x08, physicalAddr.get(4));
		mem.setByte(offset + 0x09, physicalAddr.get(5));
		mem.setInt(offset + 0x0C, (int) (logicalAddr & 0xFFFFFFFF));
		mem.setInt(offset + 0x10, (int) ((logicalAddr >> 32) & 0xFFFFFFFF));
		mem.setInt(offset + 0x14, rxRing.getAddressAs32());
		mem.setInt(offset + 0x18, txRing.getAddressAs32());
	}

	private byte getEncodedRingLength(int ringLength) {
		byte encoded = 0;
		while (ringLength != 1) {
			ringLength = ringLength >> 1;
			encoded += 1;
		}
		return (byte) (encoded << 4);
	}

	public void dumpData(Logger out) {
		out.debug("Intialization Block - 32 bit mode");
		for (int i = 0; i <= INIT_BLOCK_SIZE - 1; i += 4) {
			out.debug(
				"0x"
					+ NumberUtils.hex(
						VmAddress.as32bit(mem.getAddress()) + offset + i)
					+ " : 0x"
					+ NumberUtils.hex((byte) i)
					+ " : 0x"
					+ NumberUtils.hex(mem.getInt(offset + i)));
		}
	}
}

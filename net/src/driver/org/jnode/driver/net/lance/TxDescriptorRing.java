/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.system.MemoryResource;

/**
 * @author Chris Cole
 *
 */
public class TxDescriptorRing extends DescriptorRing {
	/** My logger */
	private final Logger log = Logger.getLogger(getClass());

	private TxDescriptor[] txDescriptors;

	public TxDescriptorRing(
		MemoryResource mem,
		int offset,
		int length,
		int dataBufferOffset) {
			
		super(mem, offset, length);

		txDescriptors = new TxDescriptor[length];

		for (int i = 0; i < length; i++) {
			txDescriptors[i] =
				new TxDescriptor(
					mem,
					offset + (i * Descriptor.MESSAGE_DESCRIPTOR_SIZE),
					dataBufferOffset + (i * BufferManager.DATA_BUFFER_SIZE));
		}

	}

	public void transmit(SocketBuffer skbuf) {
		TxDescriptor des = txDescriptors[currentDescriptor];
		if(des.isOwnerSelf()) {
			des.transmit(skbuf);
			currentDescriptor += 1;
			if(currentDescriptor == length)
				currentDescriptor = 0;
			//log.info("Setup transmit descriptor with data of size " + skbuf.getSize());
		} else {
			log.error("Not owner of descriptor index " + currentDescriptor);
		}
	}

	private TxDescriptor getAvailableDescriptor() {
		for (int i = 0; i < length; i++) {
			if (txDescriptors[i].isOwnerSelf())
				return txDescriptors[i];
		}
		return null;
	}

	public void dumpData(PrintStream out) {
		out.println("Transmit Ring Descriptors - 16 bit mode");
		for (int i = 0; i < length; i++) {
			out.println("Descriptor " + i);
			txDescriptors[i].dumpData(out);
		}
	}

}

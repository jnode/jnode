/*
 * $Id$
 */
package org.jnode.driver.net._3c90x;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.vm.Address;

/**
 * @author epr
 */
public class _3c90xRxRing implements _3c90xConstants {
	
	private static final int UPD_SIZE = 16;
	private static final int FRAME_SIZE = EthernetConstants.ETH_FRAME_LEN;
	
	/** The #frames in this ring */
	private final int nrFrames;
	/** The actual data */
	private final byte[] data;
	/** MemoryResource mapper around data */
	private final MemoryResource mem;
	/** Offset within mem of first UDP */
	private final int firstUPDOffset;
	/** Offset within mem of first ethernet frame */
	private final int firstFrameOffset;
	/** 32-bit address first UDP */
	private final Address firstUPDAddress;
	/** 32-bit address of first ethernet frame */
	private final Address firstFrameAddress;
	
	/**
	 * Create a new instance
	 * @param nrFrames The number of complete ethernet frames in this ring.
	 * @param rm
	 */
	public _3c90xRxRing(int nrFrames, ResourceManager rm) {
		
		// Create a large enough buffer
		final int size = (nrFrames * (UPD_SIZE + FRAME_SIZE)) + 16/*alignment*/;
		this.data = new byte[size];
		this.nrFrames = nrFrames;
		this.mem = rm.asMemoryResource(data);
		
		final Address memAddr = mem.getAddress();
		int addr = Address.as32bit(memAddr);
		int offset = 0;
		// Align on 16-byte boundary
		while ((addr & 15) != 0) {
			addr++;
			offset++;
		}
		
		this.firstUPDOffset = offset;
		this.firstUPDAddress = Address.add(memAddr, firstUPDOffset);
		this.firstFrameOffset = firstUPDOffset + (nrFrames * UPD_SIZE);
		this.firstFrameAddress = Address.add(memAddr, firstFrameOffset);
	}
	
	/**
	 * Initialize this ring to its default (empty) state
	 */
	public void initialize() {
		// Setup each UPD
		for (int i = 0; i < nrFrames; i++) {
			final int updOffset = firstUPDOffset + (i * UPD_SIZE);
			// Set next UPD ptr
			if (i+1 < nrFrames) {
				mem.setInt(updOffset+0, Address.as32bit(firstUPDAddress) + ((i+1) * UPD_SIZE));
			} else {
				mem.setInt(updOffset+0, Address.as32bit(firstUPDAddress));
			}
			// Set pkt status
			mem.setInt(updOffset+4, 0);
			// Set fragment address
			mem.setInt(updOffset+8, Address.as32bit(firstFrameAddress) + (i * FRAME_SIZE));
			// Set fragment size
			mem.setInt(updOffset+12, FRAME_SIZE | (1<<31));
		}
	}
	
	/**
	 * Gets the packet status of the UPD at the given index
	 * @param index
	 */
	public int getPktStatus(int index) {
		final int updOffset = firstUPDOffset + (index * UPD_SIZE);
		return mem.getInt(updOffset+4);
	}
	
	/**
	 * Sets the packet status of the UPD at the given index
	 * @param index
	 * @param value The new pkt status value
	 */
	public void setPktStatus(int index, int value) {
		final int updOffset = firstUPDOffset + (index * UPD_SIZE);
		mem.setInt(updOffset+4, value);
	}
	
	/**
	 * Gets the packet data of UPD with the given index into a SocketBuffer
	 * @param index
	 */
	public SocketBuffer getPacket(int index) {
		final int updOffset = firstUPDOffset + (index * UPD_SIZE);
		final int frameOffset = firstFrameOffset + (index * FRAME_SIZE);
		final int pktStatus = mem.getInt(updOffset+4);
		final int pktLen = pktStatus & upPktLenMask;
		final SocketBuffer skbuf = new SocketBuffer();
		skbuf.append(data, frameOffset, pktLen);
		return skbuf;
	}
	
	/**
	 * Gets the address of the first UPD of this ring.
	 */
	public Address getFirstUPDAddress() {
		return firstUPDAddress;
	}

	/**
	 * Gets the number of frames of this ring
	 */
	public int getNrFrames() {
		return nrFrames;
	}
}

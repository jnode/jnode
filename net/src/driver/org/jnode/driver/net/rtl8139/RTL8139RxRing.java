/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver.net.rtl8139;

import org.jnode.net.SocketBuffer;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.vmmagic.unboxed.Address;

/**
 * @author Martin Husted Hartvig
 */

public class RTL8139RxRing implements RTL8139Constants {

    private static final int UPD_SIZE = 16;

    // private static final int FRAME_SIZE = MAX_ETH_FRAME_LEN;

    /**
     * The #frames in this ring
     */
    private final int nrFrames;

    /**
     * The actual data
     */
    private final byte[] data;

    /**
     * MemoryResource mapper around data
     */
    private final MemoryResource mem;

    /**
     * Offset within mem of first UDP
     */
    private final int firstUPDOffset;

    /**
     * 32-bit address first UDP
     */
    private final Address firstUPDAddress;

    private int index;

    /**
     * Create a new instance
     *
     * @param nrFrames The number of complete ethernet frames in this ring.
     * @param rm
     */

    public RTL8139RxRing(int nrFrames, ResourceManager rm) {

        // Create a large enough buffer
        final int size = TOTAL_RX_BUF_SIZE;// (nrFrames * (UPD_SIZE +
        // FRAME_SIZE)) + 16/*alignment*/;
        this.data = new byte[size];
        this.nrFrames = nrFrames;
        this.mem = rm.asMemoryResource(data);

        final Address memAddr = mem.getAddress();
        int offset = 0;

        this.firstUPDOffset = offset;
        this.firstUPDAddress = memAddr.add(firstUPDOffset);
    }

    /**
     * Initialize this ring to its default (empty) state
     */

    public void initialize() {
        index = 0;
    }

    /**
     * Gets the packet status of the UPD at the given index
     */
    public int getPktStatus() {
        final int updOffset = firstUPDOffset + index;

        return mem.getInt(updOffset);
    }

    /**
     * Sets the packet status of the UPD at the given index
     *
     * @param index
     * @param value The new pkt status value
     */
    public void setPktStatus(int index, int value) {
        final int updOffset = firstUPDOffset + (index * UPD_SIZE) - 4;

        mem.setInt(updOffset + 4, value);

        this.index = index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Gets the packet data of UPD with the current index into a SocketBuffer
     */

    public SocketBuffer getPacket(int _length) {

        final int updOffset = firstUPDOffset + index;
        final SocketBuffer skbuf = new SocketBuffer();

        if (_length > 0) {
            skbuf.append(data, updOffset + 4, _length - 4);
        }

        index = (index + _length + 4 + 3) & ~3;
        index &= (RX_BUF_SIZE - 1);

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

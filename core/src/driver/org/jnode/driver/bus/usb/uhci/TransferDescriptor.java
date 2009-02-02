/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.bus.usb.uhci;

import org.jnode.system.ResourceManager;
import org.jnode.util.NumberUtils;

/**
 * A wrapper of the UHCI Transfer Descriptor layout.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class TransferDescriptor extends AbstractTreeStructure implements UHCIConstants {

    /**
     * The transfer data buffer
     */
    private final byte[] dataBuffer;
    /**
     * Offset in the data buffer
     */
    private final int dataBufferOffset;
    /**
     * The link pointer
     */
    private AbstractTreeStructure linkPointer;
    /**
     * Is the link a TD
     */
    private boolean linkIsTD;
    /**
     * The original ctrl value
     */
    private final int origCtrl;

    /**
     * Create a new instance
     *
     * @param rm               The resource manager
     * @param deviceAddress    The destination device address
     * @param endPt            The destination endpoint address
     * @param packetId         The packet id.
     * @param data0            Data Toggle, if true data0, otherwise data1
     * @param dataBuffer       The data buffer
     * @param dataBufferOffset The offset within the databuffer
     * @param isochronous      Isochronous transfer yes/no
     * @param lowspeed         Lowspeed device yes/no
     * @param ioc              Interrupt on complete yes/no
     */
    public TransferDescriptor(
        ResourceManager rm,
        int deviceAddress,
        int endPt,
        int packetId,
        boolean data0,
        byte[] dataBuffer,
        int dataBufferOffset,
        int bufLength,
        boolean isochronous,
        boolean lowspeed,
        boolean ioc) {
        super(rm, 32, 16);
        this.dataBuffer = dataBuffer;
        this.dataBufferOffset = dataBufferOffset;
        final int maxLen;
        if (bufLength > 0) {
            maxLen = bufLength - 1;
        } else {
            maxLen = 0x7FF;
        }
        // Set Linkpointer to invalid
        setInt(0, 1);
        // Set Control & Status
        int ctrl = TD_CTRL_C_ERR_MASK | TD_CTRL_ACTIVE | TD_CTRL_SPD;
        if (ioc) {
            ctrl |= TD_CTRL_IOC;
        }
        if (isochronous) {
            ctrl |= TD_CTRL_IOS;
        }
        if (lowspeed) {
            ctrl |= TD_CTRL_LS;
        }
        this.origCtrl = ctrl;
        setInt(4, ctrl); //
        // Set Token
        int token = packetId & 0xFF;
        token |= (deviceAddress & 0x7F) << 8;
        token |= (endPt & 0xF) << 15;
        token |= (data0 ? 0 : 1) << 19;
        token |= (maxLen << 21);
        setInt(8, token);
        // Set Buffer pointer
        if (dataBuffer == null) {
            setInt(12, 0);
        } else {
            setInt(12, dataBufferOffset + rm.asMemoryResource(dataBuffer).getAddress().toInt());
        }
    }

    /**
     * Reset thestatus of this TD.
     * The Active bit is set, the Error bits are cleared, the Error Counter
     * is set.
     * The SPD, LS, ISO and IOC bits are preserved.
     */
    public final void resetStatus() {
        setInt(4, origCtrl);
    }

    /**
     * Set the link pointer to the given transfer descriptor.
     *
     * @param td
     */
    public final void setLink(TransferDescriptor td, boolean depthFirst) {
        final int ptr = td.getDescriptorAddress() & 0xFFFFFFF0 | (depthFirst ? 0x4 : 0x0);
        this.linkPointer = td;
        this.linkIsTD = true;
        setInt(0, ptr);
    }

    /**
     * Set the link pointer to the given queue head.
     *
     * @param qh
     * @param depthFirst If true, the next queue is processed depth first, otherwise the next queue is
     *                   processed breadth first
     */
    public final void setLink(QueueHead qh, boolean depthFirst) {
        final int ptr = (qh.getDescriptorAddress() & 0xFFFFFFF0) | 0x2 | (depthFirst ? 0x4 : 0x0);
        this.linkPointer = qh;
        this.linkIsTD = false;
        setInt(0, ptr);
    }

    /**
     * Set the link pointer as invalid
     */
    public final void removeLink() {
        setInt(0, 1);
        this.linkIsTD = false;
        this.linkPointer = null;
    }

    /**
     * Set the active bit of this descriptor
     *
     * @param active
     */
    public final void setActive(boolean active) {
        int ctrl = getInt(4);
        if (active) {
            ctrl |= TD_CTRL_ACTIVE;
        } else {
            ctrl &= ~TD_CTRL_ACTIVE;
        }
        setInt(4, ctrl);
    }

    /**
     * Is the active bit of this descriptor set.
     */
    public final boolean isActive() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_ACTIVE) != 0);
    }

    /**
     * Is the stalled bit of this descriptor set.
     */
    public final boolean isStalled() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_STALLED) != 0);
    }

    /**
     * Is the data buffer error bit of this descriptor set.
     */
    public final boolean isDataBufferError() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_DBUFERR) != 0);
    }

    /**
     * Is the babble detected bit of this descriptor set.
     */
    public final boolean isBabbleDetected() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_BABBLE) != 0);
    }

    /**
     * Is the NAK received bit of this descriptor set.
     */
    public final boolean isNAKReceived() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_NAK) != 0);
    }

    /**
     * Is the CRC/Time Out Error bit of this descriptor set.
     */
    public final boolean isCRCTimeOutError() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_CRCTIMEO) != 0);
    }

    /**
     * Is the Bitstuff Error bit of this descriptor set.
     */
    public final boolean isBitstuffError() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_BITSTUFF) != 0);
    }

    /**
     * Is any of the error flags set.
     */
    public final boolean isAnyError() {
        final int ctrl = getInt(4);
        return ((ctrl & TD_CTRL_ANY_ERROR) != 0);
    }

    /**
     * Gets the actual length that is set by the HC.
     */
    public final int getActualLength() {
        final int len = getInt(4) & TD_CTRL_ACTLEN_MASK;
        if (len == 0x7FF) {
            return 0;
        } else {
            return len + 1;
        }
    }

    /**
     * @return Returns the dataBuffer.
     */
    public final byte[] getDataBuffer() {
        return this.dataBuffer;
    }

    /**
     * @return Returns the offset in the dataBuffer.
     */
    public final int getDataBufferOffset() {
        return this.dataBufferOffset;
    }

    /**
     * @return Returns the linkPointer.
     */
    public final AbstractTreeStructure getLink() {
        return this.linkPointer;
    }

    /**
     * Gets the linked TransferDescriptor, if any.
     *
     * @return The linked TransferDescriptor, or null if the link is null or not a TD.
     */
    public final TransferDescriptor getNextTD() {
        if (linkIsTD) {
            return (TransferDescriptor) this.linkPointer;
        } else {
            return null;
        }
    }

    /**
     * Append the given TD to the end of the list I'm a part of.
     *
     * @param td
     */
    public final void append(TransferDescriptor td, boolean depthFirst) {
        AbstractTreeStructure ptr = this;
        while (ptr instanceof TransferDescriptor) {
            final TransferDescriptor ptrTD = (TransferDescriptor) ptr;
            if (ptrTD.linkPointer == null) {
                ptrTD.setLink(td, depthFirst);
                return;
            } else {
                ptr = ptrTD.getLink();
            }
        }
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "TD[" + NumberUtils.hex(getInt(0)) + ", " + NumberUtils.hex(getInt(4)) + ", " +
            NumberUtils.hex(getInt(8)) + ", " + NumberUtils.hex(getInt(12)) + "->" + linkPointer + "]";
    }
}

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

package org.jnode.driver.bus.usb.uhci;

import org.jnode.system.ResourceManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FrameList extends AbstractStructure {

    /**
     * Shadow list
     */
    private final AbstractStructure[] list = new AbstractStructure[1024];

    /**
     * Create a new instance
     *
     * @param rm
     */
    public FrameList(ResourceManager rm) {
        super(rm, 4096, 4096);
        for (int i = 0; i < 1024; i++) {
            setListPointerInvalid(i);
        }
    }

    /**
     * Set the list pointer to the given transfer descriptor.
     *
     * @param frame The framenumber 0..1023
     * @param td
     */
    public final void setListPointer(int frame, TransferDescriptor td) {
        final int ptr = td.getDescriptorAddress() & 0xFFFFFFF0;
        this.list[frame] = td;
        setInt(frame << 2, ptr);
    }

    /**
     * Set the list pointer to the given queue head.
     *
     * @param frame      The framenumber 0..1023
     * @param qh
     * @param depthFirst If true, the next queue is processed depth first, otherwise the next queue is
     *                   processed breadth first
     */
    public final void setListPointer(int frame, QueueHead qh, boolean depthFirst) {
        final int ptr = (qh.getDescriptorAddress() & 0xFFFFFFF0) | 0x2 | (depthFirst ? 0x4 : 0x0);
        this.list[frame] = qh;
        setInt(frame << 2, ptr);
    }

    /**
     * Set the list pointer as invalid
     *
     * @param frame The framenumber 0..1023
     */
    public final void setListPointerInvalid(int frame) {
        setInt(frame << 2, 0);
        this.list[frame] = null;
    }

    /**
     * Get the list pointer at a given frame
     *
     * @param frame The framenumber 0..1023
     * @return A TransferDescriptor, a QueueHead or null.
     */
    public final AbstractStructure getListPointer(int frame) {
        return this.list[frame];
    }

    /**
     * Gets the number of elements in this list.
     */
    public final int getSize() {
        return 1024;
    }
}

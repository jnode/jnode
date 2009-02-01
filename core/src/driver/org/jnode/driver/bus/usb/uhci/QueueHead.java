/*
 * $Id$
 *
 * JNode.org
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
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class QueueHead extends AbstractTreeStructure {

    /**
     * Horizontal link
     */
    private AbstractTreeStructure linkPointer;
    /** Vertical link is updated by HC, so don't keep here */

    /**
     * Create a new instance
     *
     * @param rm
     */
    public QueueHead(ResourceManager rm) {
        super(rm, 32, 16);
        // Set Linkpointer to invalid
        setInt(0, 1);
        // Set Element Linkpointer to invalid
        setInt(4, 1);
    }

    /**
     * Set the link pointer to the given transfer descriptor.
     *
     * @param td
     */
    public final void setLink(TransferDescriptor td) {
        final int ptr = td.getDescriptorAddress() & 0xFFFFFFF0;
        this.linkPointer = td;
        setInt(0, ptr);
    }

    /**
     * Set the link pointer to the given queue head.
     *
     * @param qh
     */
    public final void setLink(QueueHead qh) {
        final int ptr = (qh.getDescriptorAddress() & 0xFFFFFFF0) | 0x2;
        this.linkPointer = qh;
        setInt(0, ptr);
    }

    /**
     * Insert the link pointer to the given queue head.
     *
     * @param qh
     */
    public final void insertLink(QueueHead qh) {
        if (qh.getLink() != null) {
            throw new IllegalArgumentException("qh.link must be null");
        }
        qh.setInt(0, this.getInt(0));
        qh.linkPointer = this.linkPointer;
        setLink(qh);
    }

    /**
     * Set the link pointer as invalid
     */
    public final void removeLink() {
        setInt(0, 1);
        this.linkPointer = null;
    }

    /**
     * Remove a given queue from the linked list starting at this queue.
     */
    public final void removeLink(QueueHead qh) {
        QueueHead p = this;
        while (p.linkPointer != qh) {
            if (p.linkPointer instanceof QueueHead) {
                p = (QueueHead) p.linkPointer;
            } else {
                // qh not found in linked list
                return;
            }
        }
        this.linkPointer = qh.linkPointer;
        this.setInt(0, qh.getInt(0));
    }

    /**
     * @return Returns the linkPointer.
     */
    public final AbstractStructure getLink() {
        return this.linkPointer;
    }

    /**
     * Add a request to this queue.
     *
     * @param request
     */
    public final void add(UHCIRequest request) {
        if (isEmpty()) {
            //log.debug("execute request");
            setElement(request.getFirstTD());
        } else {
            throw new IllegalStateException("Cannot add to non-empty queue");
        }
    }

    /**
     * Remove a request from this queue.
     *
     * @param request
     */
    public final void remove(UHCIRequest request) {
        TransferDescriptor td = request.getFirstTD();
        while (td != null) {
            if (isElement(td)) {
                removeElement();
                // Break the loop, we're done
                td = null;
            } else {
                td = td.getNextTD();
            }
        }
    }

    /**
     * Set the element link pointer to the given transfer descriptor.
     *
     * @param td
     */
    final void setElement(TransferDescriptor td) {
        final int ptr = td.getDescriptorAddress() & 0xFFFFFFF0;
        setInt(4, ptr);
    }

    /**
     * Is the element link pointer pointing to the given transfer descriptor.
     *
     * @param td
     */
    private final boolean isElement(TransferDescriptor td) {
        final int cur = getInt(4);
        final int ptr = td.getDescriptorAddress() & 0xFFFFFFF0;
        return ((cur & 1) == 0) && ((cur & 0xFFFFFFF0) == ptr);
    }

    /**
     * Set the element link pointer to the given queue head.
     * @param qh
     */
    /*private final void setElement(QueueHead qh) {
         final int ptr = (qh.getDescriptorAddress() & 0xFFFFFFF0) | 0x2;
         setInt(4, ptr);
     }*/

    /**
     * Set the element link pointer as invalid
     */
    private final void removeElement() {
        setInt(4, 1);
    }

    /**
     * Does this queue have no elements.
     *
     * @return True if this queue has not elements (T-bit=1), false otherwise.
     */
    public final boolean isEmpty() {
        final int v = getInt(4);
        return ((v & 1) == 1);
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "QH[" + NumberUtils.hex(getInt(0)) + ", " + NumberUtils.hex(getInt(4)) + "]";
    }
}

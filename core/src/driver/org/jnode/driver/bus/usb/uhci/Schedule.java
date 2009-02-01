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

import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.system.ResourceManager;

/**
 * Schedule of TransferDescriptors and QueueHeads. This is the class that constructs and maintains
 * the frame schedule.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Schedule implements USBConstants {

    /**
     * The frame list
     */
    private final FrameList frameList;
    /**
     * Interrupt queues interval 128ms
     */
    private final QueueHead int128QH;
    /**
     * Interrupt queues interval 64ms
     */
    private final QueueHead int64QH;
    /**
     * Interrupt queues interval 32ms
     */
    private final QueueHead int32QH;
    /**
     * Interrupt queues interval 16ms
     */
    private final QueueHead int16QH;
    /**
     * Interrupt queues interval 8ms
     */
    private final QueueHead int8QH;
    /**
     * Interrupt queues interval 4ms
     */
    private final QueueHead int4QH;
    /**
     * Interrupt queues interval 2ms
     */
    private final QueueHead int2QH;
    /**
     * Interrupt queues interval 1ms
     */
    private final QueueHead int1QH;
    /**
     * Low speed control queue
     */
    private final QueueHead lowSpeedControlQH;
    /**
     * High speed control queue
     */
    private final QueueHead highSpeedControlQH;
    /**
     * Bulk queue
     */
    private final QueueHead bulkQH;
    /**
     * Termination QH
     */
    private final QueueHead termQH;
    /**
     * Termination TD
     */
    private final TransferDescriptor termTD;

    /**
     * Create a new instance
     *
     * @param rm
     */
    public Schedule(ResourceManager rm) {
        //this.intQH = new QueueHead[8];
        this.frameList = new FrameList(rm);
        this.int128QH = new QueueHead(rm);
        this.int64QH = new QueueHead(rm);
        this.int32QH = new QueueHead(rm);
        this.int16QH = new QueueHead(rm);
        this.int8QH = new QueueHead(rm);
        this.int4QH = new QueueHead(rm);
        this.int2QH = new QueueHead(rm);
        this.int1QH = new QueueHead(rm);
        this.lowSpeedControlQH = new QueueHead(rm);
        this.highSpeedControlQH = new QueueHead(rm);
        this.bulkQH = new QueueHead(rm);
        this.termQH = new QueueHead(rm);
        this.termTD = new TransferDescriptor(rm, 0x7F, 0, USB_PID_IN, true, null, 0, 0, false, false, false);

        int128QH.setLink(int64QH);
        int64QH.setLink(int32QH);
        int32QH.setLink(int16QH);
        int16QH.setLink(int8QH);
        int8QH.setLink(int4QH);
        int4QH.setLink(int2QH);
        int2QH.setLink(int1QH);
        int1QH.setLink(lowSpeedControlQH);
        lowSpeedControlQH.setLink(highSpeedControlQH);
        highSpeedControlQH.setLink(bulkQH);
        bulkQH.setLink(termQH);
        // Loop back term QH to high speed control queue.
        termQH.setLink(highSpeedControlQH);
        // Maybe this helps, taken from the Linux kernel as a bugfix
        // for some Intel PIIX controllers
        termQH.setElement(termTD);
        termTD.setActive(false);

        // Setup the framelist
        final int max = frameList.getSize();
        for (int i = 0; i < max; i++) {
            QueueHead q = int1QH;
            if ((i & 1) != 0) {
                q = int2QH;
                if ((i & 2) != 0) {
                    q = int4QH;
                    if ((i & 4) != 0) {
                        q = int8QH;
                        if ((i & 8) != 0) {
                            q = int16QH;
                            if ((i & 16) != 0) {
                                q = int32QH;
                                if ((i & 32) != 0) {
                                    q = int64QH;
                                    if ((i & 64) != 0) {
                                        q = int128QH;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            frameList.setListPointer(i, q, true);
        }
    }

    /**
     * @return Returns the frameList.
     */
    public final FrameList getFrameList() {
        return this.frameList;
    }

    /**
     * Gets the optimal interrupt QH for a specific interval.
     *
     * @param interval
     */
    public final QueueHead getInterruptQH(int interval) {
        if (interval < 16) {
            if (interval < 4) {
                if (interval < 2) {
                    return int1QH; /* int1 for 0-1 ms */
                } else {
                    return int2QH; /* int2 for 2-3 ms */
                }
            } else if (interval < 8) {
                return int4QH; /* int4 for 4-7 ms */
            } else {
                return int8QH; /* int8 for 8-15 ms */
            }
        } else if (interval < 64) {
            if (interval < 32) {
                return int16QH; /* int16 for 16-31 ms */
            } else {
                return int32QH; /* int32 for 32-63 ms */
            }
        } else if (interval < 128) {
            return int64QH; /* int64 for 64-127 ms */
        } else {
            return int128QH; /* int128 for 128-255 ms (Max.) */
        }
    }

    public String toString() {
        return "Schedule{lowQH:" + lowSpeedControlQH + ", highQH:" + highSpeedControlQH + "}";
    }

    /**
     * @return Returns the bulkQH.
     */
    public QueueHead getBulkQH() {
        return this.bulkQH;
    }

    /**
     * @return Returns the highSpeedControlQH.
     */
    public QueueHead getHighSpeedControlQH() {
        return this.highSpeedControlQH;
    }

    /**
     * @return Returns the lowSpeedControlQH.
     */
    public QueueHead getLowSpeedControlQH() {
        return this.lowSpeedControlQH;
    }

}

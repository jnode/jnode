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

import java.util.ArrayList;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.bus.usb.EndPointDescriptor;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBPipe;
import org.jnode.system.ResourceManager;

/**
 * List of open pipes.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIPipeManager implements USBConstants, DeviceListener {

    /**
     * The resource manager
     */
    private final ResourceManager rm;
    /**
     * The list of pipes
     */
    private final ArrayList<USBPipe> pipes = new ArrayList<USBPipe>();
    /**
     * The schedule
     */
    private final Schedule schedule;

    /**
     * Initialize this instance
     */
    public UHCIPipeManager(ResourceManager rm, Schedule schedule) {
        this.rm = rm;
        this.schedule = schedule;
    }

    /**
     * Create a new default control pipe for a given device.
     *
     * @param device
     * @return The new pipe.
     */
    public USBControlPipe createDefaultControlPipe(USBDevice device) {
        // Add a listener here, so we can close all pipes
        // when the device stops
        device.addListener(this);

        final QueueHead skelQH;
        if (device.isLowSpeed()) {
            skelQH = schedule.getLowSpeedControlQH();
        } else {
            skelQH = schedule.getHighSpeedControlQH();
        }
        return new UHCIControlPipe(this, rm, device, null, skelQH);
    }

    /**
     * Create a new pipe for a given endpoint.
     *
     * @param endPoint
     * @return The new pipe.
     */
    public USBPipe createPipe(USBEndPoint endPoint) {
        final EndPointDescriptor descr = endPoint.getDescriptor();
        final USBDevice device = endPoint.getDevice();
        final QueueHead skelQH;
        switch (descr.getTransferType()) {
            case USB_ENDPOINT_XFER_CONTROL:
                if (device.isLowSpeed()) {
                    skelQH = schedule.getLowSpeedControlQH();
                } else {
                    skelQH = schedule.getHighSpeedControlQH();
                }
                return new UHCIControlPipe(this, rm, device, endPoint, skelQH);
            case USB_ENDPOINT_XFER_INT:
                skelQH = schedule.getInterruptQH(descr.getInterval());
                return new UHCIDataPipe(this, rm, device, endPoint, skelQH);
            case USB_ENDPOINT_XFER_BULK:
                skelQH = schedule.getBulkQH();
                return new UHCIDataPipe(this, rm, device, endPoint, skelQH);
            default:
                throw new IllegalArgumentException("Unknown/implemented transfer type");
        }
    }

    /**
     * Add a pipe to this list.
     *
     * @param pipe
     */
    final synchronized void add(UHCIPipe pipe) {
        pipes.add(pipe);
    }

    /**
     * Remove a pipe this list.
     *
     * @param pipe
     */
    final synchronized void remove(UHCIPipe pipe) {
        pipes.remove(pipe);
    }

    /**
     * An interrupt has occurred, let all requests in this list process it and remove those request
     * that are ready from the list.
     */
    public synchronized void handleInterrupt() {
        final int max = pipes.size();
        for (int i = 0; i < max; i++) {
            final UHCIPipe pipe = (UHCIPipe) pipes.get(i);
            pipe.handleInterrupt();
        }
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public void deviceStarted(Device device) {
        // Do nothing
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public synchronized void deviceStop(Device device) {
        final int max = pipes.size();
        for (int i = 0; i < max; i++) {
            final UHCIPipe pipe = (UHCIPipe) pipes.get(i);
            if (pipe.device == device) {
                pipe.close();
            }
        }
    }
}

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

import org.jnode.driver.bus.usb.SetupPacket;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.driver.bus.usb.spi.AbstractUSBControlRequest;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIControlRequest extends AbstractUSBControlRequest implements UHCIRequest, USBConstants {

    /**
     * The first TD of this request
     */
    private TransferDescriptor setupTD;

    /**
     * Initialize this instance
     *
     * @param setupPacket
     * @param dataPacket
     */
    public UHCIControlRequest(SetupPacket setupPacket, USBPacket dataPacket) {
        super(setupPacket, dataPacket);
    }

    public void createTDs(UHCIPipe pipe)
        throws USBException {
        final SetupPacket setupPacket = getSetupPacket();

        // The setup TD
        setupTD = pipe.createTD(USB_PID_SETUP, true, setupPacket.getData(), 0, setupPacket.getSize(), false);
        //log.debug("setupTD: " + setupTD + ", ls=" + ls);

        // Add the data TD's
        final USBPacket dataPacket = getDataPacket();
        final int transferLength = setupPacket.getLength();
        int length = transferLength;
        int offset = 0;
        final int dataPid = (setupPacket.isDirIn() ? USB_PID_IN : USB_PID_OUT);
        boolean dataToggle = false; // Start with DATA1
        final int maxPacketSize = pipe.getMaxPacketSize();
        if (maxPacketSize <= 0) {
            throw new USBException("Invalid maximum packet size " + maxPacketSize);
        }
        while (length > 0) {

            // Create the TD for this part of the data packet
            final TransferDescriptor dataTD;
            final int curlen = Math.min(length, maxPacketSize);
            dataTD = pipe.createTD(dataPid, dataToggle, dataPacket.getData(), offset, curlen, false);
            // Add the TD to the list
            setupTD.append(dataTD, false);

            // Update fields
            dataToggle = !dataToggle;
            length -= curlen;
            offset += curlen;
        }

        // Add final control packet
        final TransferDescriptor statusTD;
        final int statusPid = ((setupPacket.isDirOut() || (transferLength == 0)) ? USB_PID_IN : USB_PID_OUT);
        statusTD = pipe.createTD(statusPid, false, null, 0, 0, true);
        // Append theTD to the list
        setupTD.append(statusTD, false);
    }


    /**
     * Gets the first TD of this request.
     */
    public TransferDescriptor getFirstTD() {
        return setupTD;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "setup[" + getSetupPacket() + "]";
    }

}

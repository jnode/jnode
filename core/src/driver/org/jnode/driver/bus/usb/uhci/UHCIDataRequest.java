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

import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.driver.bus.usb.spi.AbstractUSBDataRequest;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIDataRequest extends AbstractUSBDataRequest implements UHCIRequest, USBConstants {

    /**
     * The first TD of this request
     */
    private TransferDescriptor firstTD;

    /**
     * Initialize this instance.
     *
     * @param dataPacket
     */
    public UHCIDataRequest(USBPacket dataPacket) {
        super(dataPacket);
    }

    public void createTDs(UHCIPipe pipe) {
        final USBPacket dataPacket = getDataPacket();
        int offset = 0;
        int length = dataPacket.getSize();
        final USBEndPoint ep = pipe.getEndPoint();
        final int dataPid = (ep.getDescriptor().isDirIn() ? USB_PID_IN : USB_PID_OUT);
        final int maxPacketSize = pipe.getMaxPacketSize();
        TransferDescriptor firstTD = null;

        while (length > 0) {

            // Create the TD for this part of the data packet
            final int curlen = Math.min(length, maxPacketSize);
            final TransferDescriptor dataTD;
            final boolean ioc = (curlen == length);
            dataTD = pipe.createTD(dataPid, ep.getDataToggle(), dataPacket.getData(), offset, curlen, ioc);
            // Add the TD to the list
            if (firstTD == null) {
                firstTD = dataTD;
            } else {
                firstTD.append(dataTD, false);
            }

            // Update fields
            ep.toggle();
            length -= curlen;
            offset += curlen;
        }
        this.firstTD = firstTD;
    }

    /**
     * @see org.jnode.driver.bus.usb.uhci.UHCIRequest#getFirstTD()
     */
    public final TransferDescriptor getFirstTD() {
        return firstTD;
    }
}

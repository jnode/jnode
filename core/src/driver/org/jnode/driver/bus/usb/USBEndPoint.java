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

package org.jnode.driver.bus.usb;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBEndPoint extends AbstractDeviceItem {

    /**
     * The interface I'm a part of.
     */
    private final USBInterface intf;
    /**
     * My descriptor
     */
    private final EndPointDescriptor descr;
    /**
     * The current data toggle for this endpoint True=DATA0, False=DATA1
     */
    private boolean dataToggle;
    /**
     * My pipe
     */
    private USBPipe pipe;

    public USBEndPoint(USBInterface intf, EndPointDescriptor descr) {
        super(intf.getDevice());
        this.intf = intf;
        this.descr = descr;
        this.dataToggle = true;
    }

    /**
     * Gets the descriptor.
     */
    public final EndPointDescriptor getDescriptor() {
        return this.descr;
    }

    /**
     * Gets the interface I'm a part of.
     */
    public final USBInterface getInterface() {
        return this.intf;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append("DESCR:");
        b.append(descr);
        return b.toString();
    }

    /**
     * Gets the current data toggle.
     *
     * @return True=DATA0, False=DATA1
     */
    public final boolean getDataToggle() {
        return this.dataToggle;
    }

    /**
     * Invert the data toggle.
     */
    public final void toggle() {
        dataToggle = !dataToggle;
    }

    /**
     * Issue a GET_STATUS request to this endpoint.
     *
     * @return The status returned by the endpoint.
     * @throws USBException
     */
    public final int getStatus()
        throws USBException {
        final USBPacket data = new USBPacket(2);
        final int epNum = descr.getEndPointAddress();
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(SetupPacket.createGetStatusPacket(USB_RECIP_ENDPOINT, epNum), data);
        pipe.syncSubmit(req, GET_TIMEOUT);
        return data.getShort(0);
    }

    /**
     * Issue a SET_FEATURE request to this endpoint.
     *
     * @param featureSelector
     * @throws USBException
     */
    public final void setFeature(int featureSelector)
        throws USBException {
        final int epNum = descr.getEndPointAddress();
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req =
            pipe.createRequest(SetupPacket.createSetFeaturePacket(USB_RECIP_ENDPOINT, epNum, featureSelector), null);
        pipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * Issue a CLEAR_FEATURE request to this endpoint.
     *
     * @param featureSelector
     * @throws USBException
     */
    public final void clearFeature(int featureSelector)
        throws USBException {
        final int epNum = descr.getEndPointAddress();
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req =
            pipe.createRequest(SetupPacket.createClearFeaturePacket(USB_RECIP_ENDPOINT, epNum, featureSelector), null);
        pipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * Issue a SYNC_FRAME request to this endpoint.
     *
     * @param frameNumber
     * @throws USBException
     */
    public final void syncFrame(int frameNumber)
        throws USBException {
        final USBPacket data = new USBPacket(2);
        data.setShort(0, frameNumber);
        final int epNum = descr.getEndPointAddress();
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(SetupPacket.createEndPointSyncFramePacket(epNum), data);
        pipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * @return Returns the pipe.
     */
    public final USBPipe getPipe() {
        if (this.pipe == null) {
            this.pipe = getDevice().getUSBBus().getHcApi().createPipe(this);
        }
        return this.pipe;
    }

}

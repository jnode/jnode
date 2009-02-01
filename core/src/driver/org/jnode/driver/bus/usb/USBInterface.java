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
 
package org.jnode.driver.bus.usb;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBInterface extends AbstractDeviceItem {

    /**
     * The configuration I'm a part of
     */
    private final USBConfiguration conf;
    /**
     * My descriptor
     */
    private final InterfaceDescriptor descr;
    /**
     * The endpoints
     */
    private final USBEndPoint[] endPoints;

    /**
     * Initialize this instance
     *
     * @param conf
     * @param descr
     */
    public USBInterface(USBConfiguration conf, InterfaceDescriptor descr) {
        super(conf.getDevice());
        this.conf = conf;
        this.descr = descr;
        this.endPoints = new USBEndPoint[descr.getNumEndPoints()];
    }

    /**
     * @return Returns the descr.
     */
    public final InterfaceDescriptor getDescriptor() {
        return this.descr;
    }

    /**
     * @return Returns the conf.
     */
    public final USBConfiguration getConfuration() {
        return this.conf;
    }

    /**
     * Gets a specific endpoint.
     */
    public final USBEndPoint getEndPoint(int index) {
        return this.endPoints[index];
    }

    /**
     * Sets a specific endpoint.
     */
    final void setEndPoint(int index, USBEndPoint endPoint) {
        if (this.endPoints[index] != null) {
            throw new SecurityException("Cannot overwrite a specific endpoint");
        } else {
            this.endPoints[index] = endPoint;
        }
    }

    /**
     * Issue a GET_STATUS request to this interface.
     *
     * @return The status returned by the interface.
     * @throws USBException
     */
    public final int getStatus()
        throws USBException {
        final USBPacket data = new USBPacket(2);
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(
            SetupPacket.createGetStatusPacket(USB_RECIP_INTERFACE, descr.getInterfaceNumber()), data);
        pipe.syncSubmit(req, GET_TIMEOUT);
        return data.getShort(0);
    }

    /**
     * Issue a SET_FEATURE request to this interface.
     *
     * @param featureSelector
     * @throws USBException
     */
    public final void setFeature(int featureSelector)
        throws USBException {
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(
            SetupPacket.createSetFeaturePacket(USB_RECIP_INTERFACE, descr.getInterfaceNumber(), featureSelector), null);
        pipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * Issue a CLEAR_FEATURE request to this interface.
     *
     * @param featureSelector
     * @throws USBException
     */
    public final void clearFeature(int featureSelector)
        throws USBException {
        final USBControlPipe pipe = getDevice().getDefaultControlPipe();
        final USBRequest req = pipe.createRequest(
            SetupPacket.createClearFeaturePacket(USB_RECIP_INTERFACE, descr.getInterfaceNumber(), featureSelector),
            null);
        pipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append("DESCR:");
        b.append(descr);
        b.append(", EPS{");
        for (int i = 0; i < endPoints.length; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(endPoints[i]);
        }
        b.append("}");
        return b.toString();
    }
}

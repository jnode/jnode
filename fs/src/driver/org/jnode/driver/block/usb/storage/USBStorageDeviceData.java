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

package org.jnode.driver.block.usb.storage;

import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.usb.InterfaceDescriptor;
import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBInterface;

final class USBStorageDeviceData implements USBStorageConstants {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(USBStorageDeviceData.class);
    /** */
    private USBDevice device;
    /** */
    private USBInterface usbInterface;
    /** */
    private int protocol;
    /** */
    private int subClass;
    /** */
    private ITransport transport;
    /** */
    private USBDataPipe sendControlPipe;
    /** */
    private USBDataPipe receiveControlPipe;
    /** */
    private USBDataPipe sendBulkPipe;
    /** */
    private USBDataPipe receiveBulkPipe;
    /** */
    private USBEndPoint bulkInEndPoint;
    /** */
    private USBEndPoint bulkOutEndPoint;
    /** */
    private USBEndPoint intrEndPoint;
    /** */
    private byte maxLun;

    /**
     * @param device
     * @throws DriverException
     */
    public USBStorageDeviceData(USBDevice device) throws DriverException {
        this.device = device;
        this.usbInterface = this.device.getConfiguration(0).getInterface(0);
        InterfaceDescriptor intf = this.usbInterface.getDescriptor();
        this.maxLun = 0;
        this.protocol = intf.getInterfaceProtocol();
        this.subClass = intf.getInterfaceSubClass();

        switch (this.protocol) {
            case US_PR_CBI:
                log.info("*** Set transport protocol to CONTROL/BULK/INTERRUPT");
                break;
            case US_PR_BULK:
                log.info("*** Set transport protocol to BULK ONLY");
                this.transport = new USBStorageBulkTransport(this);
                //((USBStorageBulkTransport)USBMassStorage.getTransport()).getMaxLun(usbDev);
                break;
            case US_PR_SCM_ATAPI:
                log.info("*** Set transport protocol to SCM ATAPI");
            default:
                throw new DriverException("Transport protocol not implemented.");
        }

        USBEndPoint ep;
        for (int i = 0; i < intf.getNumEndPoints(); i++) {
            ep = this.usbInterface.getEndPoint(i);
            // Is it a bulk endpoint ?
            if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_BULK) {
                // In or Out ?
                if ((ep.getDescriptor().getEndPointAddress() & USB_DIR_IN) == 0) {
                    this.bulkInEndPoint = ep;
                    log.info("*** Set bulk in endpoint");
                } else {
                    this.bulkOutEndPoint = ep;
                    log.info("*** Set bulk out endpoint");
                }
            } else if ((ep.getDescriptor().getAttributes() & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_INT) {
                this.intrEndPoint = ep;
                log.info("*** Set interrupt endpoint");
            }
        }


    }

    /**
     * @return Returns the receiveBulkPipe.
     */
    public USBDataPipe getReceiveBulkPipe() {
        return receiveBulkPipe;
    }

    /**
     * @param receiveBulkPipe The receiveBulkPipe to set.
     */
    public void setReceiveBulkPipe(USBDataPipe receiveBulkPipe) {
        this.receiveBulkPipe = receiveBulkPipe;
    }

    /**
     * @return Returns the receiveControlPipe.
     */
    public USBDataPipe getReceiveControlPipe() {
        return receiveControlPipe;
    }

    /**
     * @param receiveControlPipe The receiveControlPipe to set.
     */
    public void setReceiveControlPipe(USBDataPipe receiveControlPipe) {
        this.receiveControlPipe = receiveControlPipe;
    }

    /**
     * @return Returns the sendBulkPipe.
     */
    public USBDataPipe getSendBulkPipe() {
        return sendBulkPipe;
    }

    /**
     * @param sendBulkPipe The sendBulkPipe to set.
     */
    public void setSendBulkPipe(USBDataPipe sendBulkPipe) {
        this.sendBulkPipe = sendBulkPipe;
    }

    /**
     * @return Returns the sendControlPipe.
     */
    public USBDataPipe getSendControlPipe() {
        return sendControlPipe;
    }

    /**
     * @param sendControlPipe The sendControlPipe to set.
     */
    public void setSendControlPipe(USBDataPipe sendControlPipe) {
        this.sendControlPipe = sendControlPipe;
    }

    /**
     *
     * @param dev
     */


    /**
     * @return Returns the bulkInEndPoint.
     */
    public USBEndPoint getBulkInEndPoint() {
        return bulkInEndPoint;
    }

    /**
     * @param bulkInEndPoint The bulkInEndPoint to set.
     */
    public void setBulkInEndPoint(USBEndPoint bulkInEndPoint) {
        this.bulkInEndPoint = bulkInEndPoint;
    }

    /**
     * @return Returns the bulkOutEndPoint.
     */
    public USBEndPoint getBulkOutEndPoint() {
        return bulkOutEndPoint;
    }

    /**
     * @param bulkOutEndPoint The bulkOutEndPoint to set.
     */
    public void setBulkOutEndPoint(USBEndPoint bulkOutEndPoint) {
        this.bulkOutEndPoint = bulkOutEndPoint;
    }

    /**
     * @return Returns the intrEndPoint.
     */
    public USBEndPoint getIntrEndPoint() {
        return intrEndPoint;
    }

    /**
     * @param intrEndPoint The intrEndPoint to set.
     */
    public void setIntrEndPoint(USBEndPoint intrEndPoint) {
        this.intrEndPoint = intrEndPoint;
    }

    /**
     * @return Returns the maxLun.
     */
    public byte getMaxLun() {
        return maxLun;
    }

    /**
     * @param maxLun The maxLun to set.
     */
    public void setMaxLun(byte maxLun) {
        this.maxLun = maxLun;
    }

    /**
     * @return Returns the transport.
     */
    public ITransport getTransport() {
        return transport;
    }

    /**
     * @param transport The transport to set.
     */
    public void setTransport(ITransport transport) {
        this.transport = transport;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getSubClass() {
        return subClass;
    }

    public void setSubClass(int subClass) {
        this.subClass = subClass;
    }

    public USBDevice getDevice() {
        return device;
    }

    public void setDevice(USBDevice device) {
        this.device = device;
    }

}

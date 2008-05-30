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

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBPipe;
import org.jnode.driver.bus.usb.USBPipeListener;
import org.jnode.driver.bus.usb.USBRequest;
import org.jnode.driver.bus.usb.spi.AbstractUSBRequest;
import org.jnode.system.ResourceManager;
import org.jnode.util.NumberUtils;
import org.jnode.util.Queue;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UHCIPipe implements USBPipe, USBConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(UHCIPipe.class);
    /**
     * The pipe manager
     */
    private final UHCIPipeManager pm;
    /**
     * The resource manager
     */
    private final ResourceManager rm;
    /**
     * The transfer type of this pipe
     */
    private final int transferType;
    /**
     * The device of this pipe
     */
    protected final USBDevice device;
    /**
     * The endpoint number for this pipe
     */
    protected final int endPointNum;
    /**
     * The endpoint for this pipe
     */
    private final USBEndPoint endPoint;
    /**
     * Is this pipe open
     */
    private boolean open;
    /**
     * The skeleton QueueHead where qh will be linked to
     */
    private final QueueHead skelQH;
    /**
     * The QueueHead where my requests will be added to
     */
    private final QueueHead qh;
    /**
     * List of waiting requests
     */
    private final Queue<USBRequest> queue;
    /**
     * The active request
     */
    private UHCIRequest activeRequest;
    /**
     * My listeners
     */
    private ArrayList<USBPipeListener> listeners = new ArrayList<USBPipeListener>();
    /**
     * The maximum packet size, or -1 if not set
     */
    private final int maxPktSize;

    /**
     * Create a new instance
     *
     * @param pm
     * @param rm
     * @param device
     * @param ep
     * @param transferType
     * @param skelQH
     */
    public UHCIPipe(UHCIPipeManager pm, ResourceManager rm, USBDevice device, USBEndPoint ep, int transferType,
                    QueueHead skelQH) {
        this.pm = pm;
        this.rm = rm;
        this.device = device;
        this.endPoint = ep;
        this.transferType = transferType;
        this.endPointNum = (endPoint != null) ? endPoint.getDescriptor().getEndPointNumber() : 0;
        this.qh = new QueueHead(rm);
        this.skelQH = skelQH;
        this.queue = new Queue<USBRequest>();
        this.maxPktSize = (endPoint != null) ? endPoint.getDescriptor().getMaxPacketSize() : -1;
    }

    /**
     * Is this a control pipe.
     */
    public final boolean isControlPipe() {
        return (transferType == USB_ENDPOINT_XFER_CONTROL);
    }

    /**
     * Is this an interrupt pipe.
     */
    public final boolean isInterruptPipe() {
        return (transferType == USB_ENDPOINT_XFER_INT);
    }

    /**
     * Is this a isochronous pipe.
     */
    public boolean isIsochronousPipe() {
        return (transferType == USB_ENDPOINT_XFER_ISOC);
    }

    /**
     * Is this a bulk pipe.
     */
    public boolean isBulkPipe() {
        return (transferType == USB_ENDPOINT_XFER_BULK);
    }

    /**
     * Is this pipe open.
     */
    public final boolean isOpen() {
        return this.open;
    }

    /**
     * Open this pipe.
     *
     * @throws USBException
     */
    public void open() throws USBException {
        if (!this.open) {
            pm.add(this);
            skelQH.insertLink(qh);
            this.open = true;
        }
    }

    /**
     * Close this pipe.
     */
    public void close() {
        if (this.open) {
            skelQH.removeLink(qh);
            pm.remove(this);
            this.open = false;
        }
    }

    /**
     * Submit a given request via this pipe and return immediately.
     *
     * @param request
     */
    public synchronized void asyncSubmit(USBRequest request) throws USBException {
        if (!open) {
            throw new USBException("Not open");
        }
        if (!(request instanceof UHCIRequest)) {
            throw new IllegalArgumentException("Invalid request type (IUHCIRequest)");
        }
        if (!(request instanceof AbstractUSBRequest)) {
            throw new IllegalArgumentException("Invalid request type (AbstractUSBRequest)");
        }
        final UHCIRequest req = (UHCIRequest) request;
        final AbstractUSBRequest usbReq = (AbstractUSBRequest) request;
        usbReq.setCompleted(false);
        usbReq.setActualLength(0);
        usbReq.setStatus(0);
        if (qh.isEmpty()) {
            activateRequest(req);
        } else {
            queue.add(req);
        }
    }

    /**
     * Submit a given request via this pipe and wait for it to complete.
     *
     * @param request
     * @param timeout
     */
    public void syncSubmit(USBRequest request, long timeout) throws USBException {
        asyncSubmit(request);
        request.waitUntilComplete(timeout);
        if (!request.isCompleted()) {
            throw new USBException("Timeout on request");
        }
        final int status = request.getStatus();
        if ((status & USBREQ_ST_ERROR_MASK) != 0) {
            throw new USBException("USB error 0x" + NumberUtils.hex(status));
        }
    }

    /**
     * Add a listener to this pipe.
     *
     * @param listener
     */
    public synchronized void addListener(USBPipeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from this pipe.
     *
     * @param listener
     */
    public synchronized void removeListener(USBPipeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Handle an interrupt.
     */
    protected synchronized final void handleInterrupt() {
        if (this.open) {
            final UHCIRequest req = this.activeRequest;
            if (req != null) {
                handleInterrupt(req);
                if (req.isCompleted()) {
                    qh.remove(req);
                    this.activeRequest = null;
                    // If interrupt, then restart the interrupt
                    if (isInterruptPipe() && open) {
                        try {
                            asyncSubmit(req);
                        } catch (USBException ex) {
                            // Ignore
                        }
                    }
                }
            }
            if ((!queue.isEmpty()) && qh.isEmpty()) {
                final UHCIRequest nextReq = (UHCIRequest) queue.get();
                try {
                    activateRequest(nextReq);
                } catch (USBException ex) {
                    final AbstractUSBRequest areq = (AbstractUSBRequest) nextReq;
                    areq.setStatus(USBConstants.USBREQ_ST_BITSTUFF);
                    areq.setCompleted(true);
                    log.error("Unknown errr in activateRequest", ex);
                }
            }
        }
    }

    /**
     * Active the given request
     *
     * @param req
     */
    private final void activateRequest(UHCIRequest req)
        throws USBException {
        TransferDescriptor td = req.getFirstTD();
        if (td == null) {
            //log.debug("create");
            // It is a new request, create the TD's
            req.createTDs(this);
        } else {
            //log.debug("recycle");
            // It is a recycled request, reset the TD's
            while (td != null) {
                td.resetStatus();
                td = td.getNextTD();
            }
        }
        this.activeRequest = req;
        //log.debug("add");
        qh.add(req);
        //log.debug("activateRequest done");
    }

    /**
     * An interrupt has occurred, see if all TD's are finished and if so, notify all waiting
     * threads.
     */
    private final void handleInterrupt(UHCIRequest request) {
        //log.debug("handleInterrupt");
        // Go through all the TD's and look for active ones and error ones.
        TransferDescriptor errorTD = null;
        TransferDescriptor td = request.getFirstTD();
        int actualLength = 0;
        while ((td != null) && (errorTD == null)) {
            if (td.isAnyError()) {
                //log.debug("Found error TD");
                errorTD = td;
            } else if (td.isActive()) {
                //log.debug("Found active TD");
                return;
            } else {
                td = td.getNextTD();
            }
        }
        // Throw the corresponding exception in case of errors.
        int status = 0;
        if (errorTD != null) {
            if (errorTD.isStalled()) {
                status |= USBREQ_ST_STALLED;
            }
            if (errorTD.isDataBufferError()) {
                status |= USBREQ_ST_DATABUFFER;
            }
            if (errorTD.isBabbleDetected()) {
                status |= USBREQ_ST_BABBLE;
            }
            if (errorTD.isNAKReceived()) {
                status |= USBREQ_ST_NAK;
            }
            if (errorTD.isCRCTimeOutError()) {
                status |= USBREQ_ST_TIMEOUT;
            }
            if (errorTD.isBitstuffError()) {
                status |= USBREQ_ST_BITSTUFF;
            }
        } else {
            status |= USBREQ_ST_COMPLETED;
        }
        final AbstractUSBRequest usbReq = (AbstractUSBRequest) request;
        usbReq.setStatus(status);
        usbReq.setActualLength(actualLength);
        usbReq.setCompleted(true);
        firePipeEvent(request);
    }

    /**
     * Fire a pipe event to all my listeners
     *
     * @param request
     */
    protected void firePipeEvent(UHCIRequest request) {
        final int max = listeners.size();
        final int status = request.getStatus();
        final boolean error = ((status & USBREQ_ST_ERROR_MASK) != 0);
        if (error && isInterruptPipe()) {
            if ((status & USBREQ_ST_NAK) != 0) {
                // No interrupt, ignore event
                return;
            }
        }
        for (int i = 0; i < max; i++) {
            USBPipeListener l = (USBPipeListener) listeners.get(i);
            if (error) {
                l.requestFailed(request);
            } else {
                l.requestCompleted(request);
            }
        }
    }

    /**
     * Create a transfer descriptor for use on this pipe.
     *
     * @param packetId
     * @param data0
     * @param dataBuffer
     * @param dataBufferOffset
     * @param bufLength
     * @param ioc
     * @return The new TD
     */
    protected TransferDescriptor createTD(int packetId, boolean data0, byte[] dataBuffer, int dataBufferOffset,
                                          int bufLength, boolean ioc) {
        final int devAddr = device.getUSBDeviceId();
        final boolean iso = isIsochronousPipe();
        final boolean ls = device.isLowSpeed();
        return new TransferDescriptor(rm, devAddr, endPointNum, packetId, data0, dataBuffer, dataBufferOffset,
            bufLength, iso, ls, ioc);
    }

    /**
     * Gets the maximum packet size to use in this pipe.
     */
    protected final int getMaxPacketSize() {
        if (maxPktSize > 0) {
            return maxPktSize;
        } else {
            return device.getMaxPacketSize(endPointNum);
        }
    }

    /**
     * @return Returns the endPoint.
     */
    public USBEndPoint getEndPoint() {
        return this.endPoint;
    }

}

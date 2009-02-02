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
 
package org.jnode.driver.bus.usb.spi;

import org.jnode.driver.bus.usb.USBRequest;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AbstractUSBRequest implements USBRequest {

    private int actualLength = 0;
    private boolean completed = false;
    private int status = 0;

    /**
     * Gets the actual transfered data length.
     *
     * @return Returns the actualLength.
     */
    public int getActualLength() {
        return actualLength;
    }

    /**
     * Has this request bee completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Gets the status of as this request.
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param actualLength The actualLength to set.
     */
    public void setActualLength(int actualLength) {
        this.actualLength = actualLength;
    }

    /**
     * The status and actual must be set, before calling this method.
     *
     * @param completed The completed to set.
     */
    public synchronized void setCompleted(boolean completed) {
        this.completed = completed;
        notifyAll();
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Wait for this request to complete, or until a timeout occurs.
     *
     * @param timeout
     */
    public synchronized void waitUntilComplete(long timeout) {
        while (!isCompleted()) {
            try {
                wait(timeout);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
    }
}

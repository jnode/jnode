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
public interface USBPipe {

    /**
     * Is this a control pipe.
     */
    public boolean isControlPipe();

    /**
     * Is this an interrupt pipe.
     */
    public boolean isInterruptPipe();

    /**
     * Is this a isochronous pipe.
     */
    public boolean isIsochronousPipe();

    /**
     * Is this a bulk pipe.
     */
    public boolean isBulkPipe();

    /**
     * Is this pipe open.
     */
    public boolean isOpen();

    /**
     * Open this pipe.
     *
     * @throws USBException
     */
    public void open()
        throws USBException;

    /**
     * Close this pipe.
     */
    public void close();

    /**
     * Submit a given request via this pipe and return immediately.
     *
     * @param request
     */
    public void asyncSubmit(USBRequest request)
        throws USBException;

    /**
     * Submit a given request via this pipe and wait for it to complete.
     *
     * @param request
     * @param timeout
     */
    public void syncSubmit(USBRequest request, long timeout)
        throws USBException;

    /**
     * Add a listener to this pipe.
     *
     * @param listener
     */
    public void addListener(USBPipeListener listener);

    /**
     * Remove a listener from this pipe.
     *
     * @param listener
     */
    public void removeListener(USBPipeListener listener);
}

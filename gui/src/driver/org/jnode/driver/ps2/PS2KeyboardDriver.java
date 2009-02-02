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
 
package org.jnode.driver.ps2;

import java.nio.channels.ByteChannel;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.input.AbstractKeyboardDriver;
import org.jnode.system.IRQResource;
import org.jnode.util.NumberUtils;

/**
 * @author qades
 */
public class PS2KeyboardDriver extends AbstractKeyboardDriver implements PS2Constants {

    private final PS2Bus bus;
    private final PS2ByteChannel channel;
    private IRQResource irq;
    private static final Logger log = Logger.getLogger(PS2KeyboardDriver.class);

    PS2KeyboardDriver(PS2Bus ps2) {
        this.bus = ps2;
        this.channel = ps2.getKbChannel();
    }

    /**
     * @see org.jnode.driver.input.AbstractKeyboardDriver#getChannel()
     */
    protected ByteChannel getChannel() {
        return channel;
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected synchronized void startDevice() throws DriverException {
        // Claim the irq
        irq = bus.claimResources(getDevice(), KB_IRQ);
        try {
            // Set the mode
            setEnabled(true);
        } catch (DeviceException ex) {
            throw new DriverException("Cannot enable keyboard", ex);
        }
        // Start the rest
        super.startDevice();
        // Make sure all queues are empty
        bus.flush();
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected synchronized void stopDevice() throws DriverException {
        // Set the mode
        try {
            setEnabled(false);
        } catch (DeviceException ex) {
            log.debug("Error disabling keyboard", ex);
        }
        // Stop everything
        super.stopDevice();
        irq.release();
        irq = null;
        bus.releaseResources();
    }

    private final void setEnabled(boolean on) throws DeviceException {
        log.debug("Old mode 0x" + NumberUtils.hex(bus.getMode(), 2));
        bus.setKeyboardEnabled(on);
        log.debug("New mode 0x" + NumberUtils.hex(bus.getMode(), 2));
    }
}

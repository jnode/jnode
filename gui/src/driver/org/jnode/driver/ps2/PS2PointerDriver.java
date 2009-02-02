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

import java.io.IOException;
import java.nio.channels.ByteChannel;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.input.AbstractPointerDriver;
import org.jnode.driver.input.PointerInterpreter;
import org.jnode.system.IRQResource;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author qades
 */
public class PS2PointerDriver extends AbstractPointerDriver implements PS2Constants {

    private static final Logger log = Logger.getLogger(PS2PointerDriver.class);

    static final int CMD_SET_RES = 0xE8; /* Set resolution */
    static final int CMD_SET_SCALE11 = 0xE6; /* Set 1:1 scaling */
    static final int CMD_SET_SCALE21 = 0xE7; /* Set 2:1 scaling */
    static final int CMD_GET_SCALE = 0xE9; /* Get scaling factor */
    static final int CMD_SET_STREAM = 0xEA; /* Set stream mode */

    private final PS2Bus bus;
    private final PS2ByteChannel channel;
    private IRQResource irq;

    PS2PointerDriver(PS2Bus ps2) {
        this.bus = ps2;
        this.channel = ps2.getMouseChannel();
    }

    protected int getIRQ() {
        return MOUSE_IRQ;
    }

    protected boolean initPointer() throws DeviceException {
        boolean result = enablePointer();
        result &= setRate(100);
        return result;
    }

    protected boolean enablePointer() throws DeviceException {
        log.debug("enablePointer");
        return bus.writeMouseCommands(CMD_ENABLE, null, 0);
    }

    protected boolean disablePointer() throws DeviceException {
        log.debug("disablePointer");
        return bus.writeMouseCommands(CMD_DISABLE, null, 0);
    }

    protected int getPointerId() throws DriverException {
        try {
            if (!bus.writeMouseCommands(CMD_GET_ID, null, 1)) {
                throw new DriverException("Cannot request Pointer ID");
            }
            return channel.read(50);
        } catch (DeviceException ex) {
            throw new DriverException("Error in requesting Pointer ID", ex);
        } catch (IOException ex) {
            throw new DriverException("Error in requesting Pointer ID", ex);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in requesting Pointer ID", ex);
        } catch (InterruptedException ex) {
            throw new DriverException("Interrupted in requesting Pointer ID", ex);
        }
    }

    protected boolean setRate(int samples) throws DeviceException {
        return bus.writeMouseCommands(CMD_SET_RATE, new int[] {samples}, 0);
    }

    /**
     * @see org.jnode.driver.input.AbstractPointerDriver#getChannel()
     */
    protected ByteChannel getChannel() {
        return channel;
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected synchronized void startDevice() throws DriverException {
        irq = bus.claimResources(getDevice(), MOUSE_IRQ);
        try {
            // Set the mode
            setEnabled(true);
        } catch (DeviceException ex) {
            throw new DriverException("Cannot enable keyboard", ex);
        }
        super.startDevice();
        // Make sure all queues are empty
        bus.flush();
        final PointerInterpreter interpreter = getPointerInterpreter();
        if (interpreter != null) {
            interpreter.reset();
        }
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected synchronized void stopDevice() throws DriverException {
        try {
            setEnabled(false);
        } catch (DeviceException ex) {
            log.debug("Error disabling keyboard", ex);
        }
        super.stopDevice();
        irq.release();
        irq = null;
        bus.releaseResources();
    }

    private final void setEnabled(boolean on) throws DeviceException {
        log.debug("Old mode 0x" + NumberUtils.hex(bus.getMode(), 2));
        bus.setMouseEnabled(on);
        log.debug("New mode 0x" + NumberUtils.hex(bus.getMode(), 2));
    }
}

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

package org.jnode.driver.input;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;

/**
 * @author qades
 */
public abstract class AbstractPointerDriver extends AbstractInputDriver<PointerEvent> implements PointerAPI {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(AbstractPointerDriver.class);
    private ByteChannel channel;
    private PointerInterpreter interpreter;


    public void addPointerListener(PointerListener l) {
        super.addListener(l);
    }

    public void removePointerListener(PointerListener l) {
        super.removeListener(l);
    }

    public synchronized void setPreferredListener(PointerListener l) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SET_PREFERRED_LISTENER_PERMISSION);
        }
        super.setPreferredListener(l);
    }

    /**
     * Start the pointer device.
     */
    protected synchronized void startDevice() throws DriverException {
        final Device dev = getDevice();
        final String id = dev.getId();
        log.debug("Starting " + id);
        this.channel = getChannel();
        this.interpreter = createInterpreter();
        try {
            setRate(80);
        } catch (DeviceException ex) {
            log.error("Cannot set default rate", ex);
        }

        // start the deamon anyway, so we can register a mouse later
        startDispatcher(id);
        dev.registerAPI(PointerAPI.class, this);
    }

    protected PointerEvent handleScancode(byte scancode) {
        PointerEvent event = null;
        if (interpreter != null) {
            event = interpreter.handleScancode(scancode & 0xff);
        }
        return event;
    }

    protected PointerInterpreter createInterpreter() {
        log.debug("createInterpreter");
        try {
            initPointer(); // bring mouse into stable state
        } catch (DeviceException ex) {
            log.error("Cannot initialize pointer", ex);
            return null;
        }

        PointerInterpreter i = new MouseInterpreter();
        if (i.probe(this)) {
            log.info("Found " + i.getName());
            return i;
        } else {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ex1) {
                //empty
            }
            // here goes the tablet stuff
            return null;
        }
    }

    /**
     * Stop the pointer device.
     */
    protected synchronized void stopDevice() throws DriverException {
        getDevice().unregisterAPI(PointerAPI.class);
        stopDispatcher();

        try {
            channel.close();
            channel = null;
        } catch (IOException ex) {
            System.err.println("Error closing Pointer channel: " + ex.toString());
        }
    }

    /**
     * Send a given pointer event to the given listener.
     *
     * @param listener the pointer listener to recieve the event
     * @param event the pointer event
     */
    @Override
    protected void sendEvent(SystemListener listener, PointerEvent event) {
        PointerListener ml = (PointerListener) listener;
        ml.pointerStateChanged(event);
    }

    /**
     * @return PointerInterpreter
     */
    public PointerInterpreter getPointerInterpreter() {
        return interpreter;
    }

    /**
     * Sets the Interpreter.
     *
     * @param interpreter the Interpreter
     */
    public void setPointerInterpreter(PointerInterpreter interpreter) {
        if (interpreter == null)
            throw new NullPointerException();
        this.interpreter = interpreter;
    }

    protected abstract int getPointerId() throws DriverException;

    protected abstract boolean initPointer() throws DeviceException;

    protected abstract boolean enablePointer() throws DeviceException;

    protected abstract boolean disablePointer() throws DeviceException;

    protected abstract boolean setRate(int samples) throws DeviceException;

}

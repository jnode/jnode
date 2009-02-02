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
 
package org.jnode.driver.bus.i2c;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class I2cProtocol implements I2cConstants {

    private final I2cAPI api;
    private final I2cLineState state = new I2cLineState();

    /**
     * Create a new instance
     * 
     * @param api
     */
    public I2cProtocol(I2cAPI api) {
        this.api = api;
    }

    /**
     * Send a single byte
     * 
     * @param data
     * @return The acknowledgement
     * @throws I2cTimeoutException
     */
    public boolean sendByte(byte data) throws I2cTimeoutException {
        for (int i = 7; i >= 0; i--) {
            if ((data & (1 << i)) != 0) {
                high();
            } else {
                low();
            }
        }
        return getAck();
    }

    /**
     * Read a single byte
     * 
     * @param ackRequired If true, and ack will be send after the read
     * @return The byte read
     * @throws I2cTimeoutException
     */
    public int readByte(boolean ackRequired) throws I2cTimeoutException {
        int i;
        int data = 0;

        /* read data */
        api.setLines(0, 1);
        for (i = 7; i >= 0; i--) {
            api.setLines(1, 1);
            if (getData()) {
                data |= (1 << i);
            }
            api.setLines(0, 1);
        }

        /* send acknowledge */
        if (ackRequired) {
            sendAck();
        }
        return data;
    }

    /*
     * private final void start() { api.setLines(0, 1); api.setLines(1, 1);
     * api.setLines(1, 0); api.setLines(0, 0); }
     */

    /*
     * private final void stop() { api.setLines(0, 0); api.setLines(1, 0);
     * api.setLines(1, 1); api.setLines(0, 1); }
     */

    private final void high() {
        api.setLines(0, 1);
        api.setLines(1, 1);
        api.setLines(0, 1);
    }

    private final void low() {
        api.setLines(0, 0);
        api.setLines(1, 0);
        api.setLines(0, 0);
    }

    private final boolean getAck() throws I2cTimeoutException {
        api.setLines(0, 1);
        api.setLines(1, 1);
        final boolean ack = getData();
        api.setLines(0, 1);
        return ack;
    }

    private final void sendAck() {
        api.setLines(0, 0);
        api.setLines(1, 0);
        api.setLines(0, 0);
    }

    private final boolean getData() throws I2cTimeoutException {
        int count = 0;
        do {
            api.getLines(state);

            /* manage timeout */
            count++;
            if (count > I2C_TIMEOUT) {
                throw new I2cTimeoutException();
            }

            /* wait a bit, so not hammering bus */
            try {
                Thread.sleep(I2C_DELAY);
            } catch (InterruptedException ex) {
                // Ignore
            }
        } while (!state.scl); /* wait for high clock */
        return state.sda;
    }

}

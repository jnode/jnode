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

package org.jnode.driver.ps2;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.character.ChannelAlreadyOwnedException;
import org.jnode.driver.character.CharacterDeviceAPI;
import org.jnode.util.ByteQueue;

/**
 * @author qades
 */
abstract class PS2Driver extends Driver implements CharacterDeviceAPI, PS2Constants, ByteChannel {

    private final ByteQueue queue = new ByteQueue();
    private Device owner;

    protected final PS2Bus ps2;

    PS2Driver(PS2Bus ps2) {
        this.ps2 = ps2;
    }

    /**
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public void handleScancode(int b) {
        queue.push((byte) b);
    }

    /**
     * Start the PS/2 device.
     */
    protected synchronized void startDevice() throws DriverException {
        init();
        getDevice().registerAPI(CharacterDeviceAPI.class, this); // make sure
                                                                    // it's at
                                                                    // least a
                                                                    // character
                                                                    // device
    }

    /**
     * Stop the PS/2 device.
     */
    protected synchronized void stopDevice() throws DriverException {
        getDevice().unregisterAPI(CharacterDeviceAPI.class);
        deinit();
    }

    public synchronized ByteChannel getChannel(Device owner)
        throws ChannelAlreadyOwnedException, DeviceException {
        if (this.owner != null) {
            throw new ChannelAlreadyOwnedException(this.owner);
        } else {
            this.owner = owner;
            return this;
        }
    }

    public int read(ByteBuffer dst) throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }

        // ToDo: proper exception handling (if end of queue -> IOException)
        int i;
        for (i = 0; i < dst.remaining(); i++) {
            dst.put(queue.pop());
        }
        return i;
    }

    public int write(ByteBuffer b) throws NonWritableChannelException {
        throw new NonWritableChannelException();
    }

    public boolean isOpen() {
        return (owner != null);
    }

    public synchronized void close() {
        owner = null;
    }

    protected abstract void init() throws DriverException;

    protected abstract void deinit();

    abstract int getIRQ();

}

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
 
package org.jnode.apps.jpartition.utils.device;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPIHelper;

public class FakeIDEDevice extends AbstractIDEDevice {
    private final long length;

    public FakeIDEDevice(String name, boolean primary, boolean master, long length)
        throws IOException, DriverException, NameNotFoundException, UnsupportedFormatException {
        super(name, primary, master);
        this.length = length;
    }

    public void flush() throws IOException {
    }

    public long getLength() throws IOException {
        return length;
    }

    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        BlockDeviceAPIHelper.checkBounds(this, devOffset, destBuf.remaining());

        while (destBuf.remaining() > 0) {
            destBuf.put((byte) 0);
        }
    }

    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        BlockDeviceAPIHelper.checkBounds(this, devOffset, srcBuf.remaining());

    }
}

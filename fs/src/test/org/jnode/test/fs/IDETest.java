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

package org.jnode.test.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.naming.NamingException;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.naming.InitialNaming;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class IDETest {

    public static void main(String[] args)
        throws NamingException, ApiNotFoundException, IOException, DeviceNotFoundException {

        final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
        final String name = (args.length > 0) ? args[0] : "hda";

        IDEDevice dev = (IDEDevice) dm.getDevice(name);
        IDEDeviceAPI api = dev.getAPI(IDEDeviceAPI.class);
        IDEDriveDescriptor descr = dev.getDescriptor();

        System.out.println("LBA support   : " + descr.supportsLBA());
        System.out.println("DMA support   : " + descr.supportsDMA());
        System.out.println("48-bit support: " + descr.supports48bitAddressing());
        System.out.println("Length        : " + api.getLength());

        final ByteBuffer data = ByteBuffer.allocate(1024);
        api.read(0, data);

        while (data.remaining() > 0) {
            System.out.print(NumberUtils.hex(data.get(), 2) + ' ');
        }
        System.out.println();

    }
}

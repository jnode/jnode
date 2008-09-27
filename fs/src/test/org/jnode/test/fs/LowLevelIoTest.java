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

import java.nio.ByteBuffer;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.naming.InitialNaming;

/**
 * This class test basic IO on a block device.
 *
 * @author gbin
 */
public class LowLevelIoTest {

    public static void main(String[] args) {
        DeviceManager dm;
        try {
            dm = InitialNaming.lookup(DeviceManager.NAME);

            IDEDevice current = (IDEDevice) dm.getDevice(args[0]);
            BlockDeviceAPI api = current.getAPI(BlockDeviceAPI.class);

            int size = (int) (Math.random() * 5 /*256*/) * 512;
            int offset = (int) (Math.random() * 10000) * 512;
            System.out.println("Create Random Buffer");
            System.out.println("Size = " + size);
            byte[] src = new byte[size];
            for (int i = 0; i < size; i++) {
                src[i] = (byte) (Math.random() * 255);
            }

            System.out.println("Put it at " + offset);
            api.write(offset, ByteBuffer.wrap(src));

            System.out.println("Retreive it back ...");
            byte[] dest = new byte[size];
            api.read(offset, ByteBuffer.wrap(dest));

            System.out.println("Check consistency ...");
            for (int i = 0; i < size; i++) {
                System.out.print(src[i] + "|" + dest[i] + ",");
                if (src[i] != dest[i])
                    throw new Exception("Inconsistency");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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
 
package org.jnode.test.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * Invoke many read requests from multiple threads to stress the device.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BlockDeviceStressTest {

    public static void main(String[] args) throws DeviceNotFoundException,
        ApiNotFoundException, IOException, InterruptedException {
        if (args.length == 0) {
            System.out.println("specify device ids");
        } else {
            final List<BlockDeviceAPI> apis = new ArrayList<BlockDeviceAPI>();
            for (int i = 0; i < args.length; i++) {
                final String id = args[i];
                final Device device = DeviceUtils.getDevice(id);
                apis.add(device.getAPI(BlockDeviceAPI.class));
            }

            final int repeatCount = 1000;
            final List<Thread> threads = new ArrayList<Thread>();

            for (BlockDeviceAPI api : apis) {
                final int threadCount = 10;
                final long length = api.getLength();
                for (int t = 0; t < threadCount; t++) {
                    threads.add(new TestThread(api, repeatCount, length
                        / (threadCount + 2)));
                }
            }
            for (Thread t : threads) {
                t.start();
            }
        }
    }

    static class TestThread extends Thread {

        private final BlockDeviceAPI api;

        private final ByteBuffer data;

        private final int repeatCount;

        private final long offset;

        private int errors;

        public TestThread(BlockDeviceAPI api, int repeatCount, long offset) {
            this.repeatCount = repeatCount;
            this.offset = offset;
            this.api = api;
            data = ByteBuffer.allocate(4096);
        }

        public void run() {
            for (int loop = 0; loop < repeatCount; loop++) {
                try {
                    api.read(offset, data);
                } catch (IOException ex) {
                    errors++;
                }
            }
            System.out.println("Finished " + repeatCount + " with " + errors
                + " errors");
        }
    }
}

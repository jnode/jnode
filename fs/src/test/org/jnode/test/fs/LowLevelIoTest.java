/*
 * $Id$
 */
package org.jnode.test.fs;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.ide.IDEDevice;
import org.jnode.naming.InitialNaming;

/**
 * This class test basic IO on a block device.
 * @author gbin
 */
public class LowLevelIoTest {

	public static void main(String[] args) {
		DeviceManager dm;
		try {
			dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);

			IDEDevice current = (IDEDevice)dm.getDevice(args[0]);
			BlockDeviceAPI api =
				(BlockDeviceAPI)current.getAPI(BlockDeviceAPI.class);

			int size = (int) (Math.random() * 5 /*256*/) * 512;
			int offset = (int) (Math.random() * 10000) * 512;
			System.out.println("Create Random Buffer");
			System.out.println("Size = " + size);
			byte[] src = new byte[size];
			for (int i = 0; i < size; i++) {
				src[i] = (byte) (Math.random() * 255);
			}

			System.out.println("Put it at " + offset);
			api.write(offset, src, 0, size);

			System.out.println("Retreive it back ...");
			byte[] dest = new byte[size];
			api.read(offset, dest, 0, size);

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

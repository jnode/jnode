/*
 * $Id$
 */
package org.jnode.test.fs;

import java.io.IOException;

import javax.naming.NamingException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.ide.IDEDevice;
import org.jnode.driver.ide.IDEDeviceAPI;
import org.jnode.driver.ide.IDEDriveDescriptor;
import org.jnode.naming.InitialNaming;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class IDETest {

	public static void main(String[] args) 
	throws NamingException, ApiNotFoundException, IOException, DeviceNotFoundException {
		
		final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
		final String name = (args.length > 0) ? args[0] : "hda";
		
		IDEDevice dev = (IDEDevice)dm.getDevice(name);
		IDEDeviceAPI api = (IDEDeviceAPI)dev.getAPI(IDEDeviceAPI.class);
		IDEDriveDescriptor descr = dev.getDescriptor();
		
		System.out.println("LBA support   : " + descr.supportsLBA());
		System.out.println("DMA support   : " + descr.supportsDMA());
		System.out.println("48-bit support: " + descr.supports48bitAddressing());
		System.out.println("Length        : " + api.getLength());
		
		final byte[] data = new byte[1024];
		api.read(0, data, 0, data.length);
		
		for (int i = 0; i < data.length; i++) {
			System.out.print(NumberUtils.hex(data[i], 2) + ' ');
		}
		System.out.println();
		
	}
}

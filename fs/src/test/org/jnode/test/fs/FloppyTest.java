/*
 * $Id$
 */
package org.jnode.test.fs;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.naming.InitialNaming;

/**
 * @author epr
 */
public class FloppyTest {
	
	/** My logger */
	private static final Logger log = Logger.getLogger(FloppyTest.class);
	
	public static void main(String[] args) { 
		
		try {
			final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			final Device fd0 = dm.getDevice ("fd0");
			final BlockDeviceAPI api = (BlockDeviceAPI)fd0.getAPI(BlockDeviceAPI.class);
			try {
				
				final byte[] buf = new byte[512];
				api.read(0, buf, 0, buf.length);
			} catch (IOException ex) {
				log.error("Oops", ex);
			}
		} catch (ApiNotFoundException ex) {
			log.error("BlockDeviceAPI not found", ex);
		} catch (DeviceNotFoundException ex) {
			log.error("fd0 device not found", ex);
		} catch (NameNotFoundException ex) {
			log.error("device manager not found", ex);
		}
	}

}

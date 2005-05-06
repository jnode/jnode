/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.test.fs;

import java.io.IOException;
import java.nio.ByteBuffer;

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
				
				final ByteBuffer buf = ByteBuffer.allocate(512);
				api.read(0, buf);
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

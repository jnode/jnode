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
 
package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnode.naming.InitialNaming;
import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;

/**
 * @author qades
 */
public class DeviceArgument extends Argument {

	public DeviceArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public DeviceArgument(String name, String description) {
		super(name, description);
	}

	public Device getDevice(ParsedArguments args) throws SyntaxErrorException {
		String value = getValue(args);
		try {
			return ((DeviceManager)InitialNaming.lookup(DeviceManager.NAME)).getDevice(value);
		} catch(NameNotFoundException ex) {
			throw new SyntaxErrorException("DeviceManager not found. Check your system setup");
		} catch(DeviceNotFoundException ex) {
			throw new SyntaxErrorException("Device " + value + " not found");
		}
	}

	public String complete(String partial) {
		List devices = new ArrayList();
                try {
			// get the alias manager
			final DeviceManager devMgr = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);

                        // collect matching aliases
			Iterator i = devMgr.getDevices().iterator();
			while( i.hasNext() ) {
				String dev = ((Device)i.next()).getId();
				if( dev.startsWith(partial) )
					devices.add(dev);
			}
			return complete(partial, devices);
		} catch( NameNotFoundException ex ) {
			// should not happen!
			return partial;
		}
	}

}

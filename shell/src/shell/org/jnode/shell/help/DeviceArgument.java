/*
 * $Id$
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

	public Device getDevice(ParsedArguments args) throws SyntaxError {
		String value = getValue(args);
		try {
			return ((DeviceManager)InitialNaming.lookup(DeviceManager.NAME)).getDevice(value);
		} catch(NameNotFoundException ex) {
			throw new SyntaxError("DeviceManager not found. Check your system setup");
		} catch(DeviceNotFoundException ex) {
			throw new SyntaxError("Device " + value + " not found");
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

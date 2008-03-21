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
 
package org.jnode.shell.help.argument;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.SyntaxErrorException;

/**
 * @author qades
 */
public class DeviceArgument extends Argument {

	private final Class<? extends DeviceAPI> apiClass;

	public DeviceArgument(String name, String description, boolean multi) {
		this(name, description, null, multi);
	}

	public DeviceArgument(String name, String description) {
		this(name, description, null);
	}

	public DeviceArgument(String name, String description, Class<? extends DeviceAPI> apiClass, boolean multi) {
		super(name, description, multi);
		this.apiClass = apiClass;
	}

	public DeviceArgument(String name, String description, Class<? extends DeviceAPI> apiClass) {
		super(name, description);
		this.apiClass = apiClass;
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

	public void complete(CompletionInfo completion, String partial) {
        try {
            // get the alias manager
            final DeviceManager devMgr = InitialNaming
                    .lookup(DeviceManager.NAME);

            // collect matching aliases
            for (Device dev : devMgr.getDevices()) {
            	if( apiClass != null && !dev.implementsAPI(apiClass) )
            		continue;
            	
                final String devId = dev.getId();
                if (devId.startsWith(partial)) {
                    completion.addCompletion(devId);
                }
            }
        } catch (NameNotFoundException ex) {
            // should not happen!
            return;
        }
    }
}

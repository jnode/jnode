/*
 * $Id$
 */
package org.jnode.driver.character;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;

/**
 * @author qades
 */
public class ChannelAlreadyOwnedException extends DeviceException {
	public ChannelAlreadyOwnedException(Device owner) {
		super("Device already owned by " + owner.getId());
	}
}

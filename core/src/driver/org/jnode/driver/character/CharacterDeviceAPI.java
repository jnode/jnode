/**
 * $Id$
 */
package org.jnode.driver.character;

import java.nio.channels.ByteChannel;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DeviceException;

/**
 * @author qades
 */
public interface CharacterDeviceAPI extends DeviceAPI {

	/**
	 * Aquire the channel associated with this device.
	 * @param owner the subsequent owner of this channel
	 * @return The channel
	 * @throws ChannelAlreadyOwnedException if the channel to this device is already owned
	 * @throws DeviceException
	 */
	public ByteChannel getChannel(Device owner) throws ChannelAlreadyOwnedException, DeviceException;
}

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
 
package org.jnode.driver.sound.speaker.pc;
import org.jnode.driver.Bus;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/** The class to find a PC Speaker 
 *  @author Matt Paine
 **/
public class PCSpeakerFinder implements DeviceFinder
{

//**********  DeviceFinder implementation  **********//

	/** Finds the pc device for the device manager.
	 *  @param devMan The device manager to register the device with.
	 *  @param bus The bus to find this device on.
	 **/
	public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException
	{
		try
		{
			devMan.register(new PCSpeakerDevice(bus));
		}
		catch (DriverException dex)
		{
			throw new DeviceException(dex);
		}
	}


}


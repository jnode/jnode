
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


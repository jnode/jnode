
package org.jnode.driver.sound.speaker.pc;
import  org.jnode.driver.Bus;
import  org.jnode.driver.Device;
import  org.jnode.driver.DriverException;

/** Defines the speaker device
 *  @author Matt Paine
 **/
public class PCSpeakerDevice extends Device
{

	/** Constructs the device.
	 *  @param bus The bus that this device is on.
	 **/
	public PCSpeakerDevice (Bus bus) throws DriverException
	{
		super (bus, "speaker0");
		setDriver(new PCSpeakerDriver());
	}


}


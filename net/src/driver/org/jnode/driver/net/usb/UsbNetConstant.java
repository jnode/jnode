package org.jnode.driver.net.usb;

import org.jnode.driver.bus.usb.USBConstants;

public interface UsbNetConstant extends USBConstants {
	/* Subclass */
	public final static int US_SC_RF	=0x01;
	/* Protocols */
    public final static int US_PR_BLUETOOTH	=0x01;		/* Control/Bulk/Interrupt */
}

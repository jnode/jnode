/*
 * $Id$
 */
package org.jnode.driver.input;

public class LogitechWheelMouseProtocol extends LogitechProtocol {

	public boolean supportsId(int id) {
		return id == 3;
	}

	public String getName() {
		return "Logitech Wheel Mouse";
	}

	public int getPacketSize() {
		return 4;
	}

	public PointerEvent buildEvent(byte[] data) {
		PointerEvent e = super.buildEvent(data);

		int z = data[3];
		return new PointerEvent(e.getButtons(), e.getX(), e.getY(), z, e.isAbsolute());
	}
}

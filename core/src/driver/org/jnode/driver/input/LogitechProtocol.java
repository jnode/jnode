/*
 * $Id$
 */
package org.jnode.driver.input;

public class LogitechProtocol implements MouseProtocol {

	public boolean supportsId(int id) {
		return id == 0;
	}

	public String getName() {
		return "Logitech Mouse";
	}

	public int getPacketSize() {
		return 3;
	}

	public PointerEvent buildEvent(byte[] data) {
		int buttons = data[0] & 7; // left, middle, right

		int x = data[1] & 0xff;
		if ((data[0] & 0x10) != 0) {
			x = x - 256;
		}

		int y = data[2] & 0xff;
		if ((data[0] & 0x20) != 0) {
			y = y - 256;
		}
		return new PointerEvent(buttons, x, y, PointerEvent.RELATIVE);
	}
}

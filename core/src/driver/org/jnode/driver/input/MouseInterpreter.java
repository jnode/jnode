/*
 * $Id$
 */
package org.jnode.driver.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;

/**
 * author qades
 */
public class MouseInterpreter implements PointerInterpreter {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private static final transient List protocols = new ArrayList();

	private byte[] data; // will be defined as 3 or 4 bytes, according to the protocol
	private int pos = 0;
	MouseProtocol protocol;

	static {
		// should be configurable via an ExtensionPoint
		protocols.add(new LogitechWheelMouseProtocol());
		protocols.add(new LogitechProtocol());
	}

	public String getName() {
		if (protocol == null)
			return "No Mouse";
		return protocol.getName();
	}

	public boolean probe(AbstractPointerDriver d) {
		try {
			// reset the mouse
			if (!d.initPointer()) {
				log.debug("Reset mouse failed");
				return false;
			}
			int id = d.getPointerId();
			if (id != 0) {
				// does not seem to be a mouse, more likely a tablet of touch screen
				log.debug("PointerId == " + id);
				return false;
			}

			//int protocolBytes = 3; // standard: 3 byte protocol

			// try to make this a 3 button + wheel
			boolean result = d.setRate(200);
			result &= d.setRate(100);
			result &= d.setRate(80);
			// a "normal" mouse doesn't recognize this sequence as special
			// but a mouse with a wheel will change its mouse ID

			id = d.getPointerId();
			// select protocol
			for (Iterator i = protocols.iterator(); i.hasNext();) {
				final MouseProtocol p = (MouseProtocol) i.next();
				if (p.supportsId(id)) {
					this.protocol = p;
					break;
				}
			}
			if (protocol == null) {
				log.error("No mouse driver found for PointerID " + id);
				return false;
			}
			this.data = new byte[protocol.getPacketSize()];

			return result;
		} catch (DriverException ex) {
			log.error("Error probing for mouse", ex);
			return false;
		} catch (DeviceException ex) {
			log.error("Error probing for mouse", ex);
			return false;
		}
	}

	public PointerEvent handleScancode(int scancode) {
		if (protocol == null)
			return null;

		// build the data block
		data[pos++] = (byte) (scancode & 0xff);
		pos %= data.length;
		if (pos != 0)
			return null;
		// this debug output is for debugging the mouse protocol
		/*
		 * String line = ""; for( int i = 0; i < data.length; i++ ) line += "[0x" +
		 * Integer.toHexString(data[i]) + "]"; log.debug(line);
		 */

		PointerEvent event = protocol.buildEvent(data);
		// this debug output is to dump the pointer events
		// log.debug(event.toString());
		return event;
	}
}

/*
 * $Id$
 */
package org.jnode.test;

import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PointerTest {

	public static void main(String[] args) 
	throws Exception {
		
		final String devId = (args.length > 0) ? args[0] : "ps2mouse";
		PointerAPI api = (PointerAPI)DeviceUtils.getAPI(devId, PointerAPI.class);
		api.addPointerListener(new MyListener());
	}
	
	public static class MyListener implements PointerListener {
		
		
		
			/**
		 * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
		 */
		public void pointerStateChanged(PointerEvent event) {
			System.out.println("x,y,z abs: " + event.getX() + "," + event.getY() + "," + event.getZ() + " " + event.isAbsolute());
			// TODO Auto-generated method stub

		}

}
}

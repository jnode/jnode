package org.jnode.driver.console.textscreen;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;
import org.jnode.system.event.FocusEvent;
import org.jnode.system.event.FocusListener;
import org.jnode.util.Queue;


/**
 * @author crawley@jnode.org
 *
 */
public class DefaultKeyboardHandler extends KeyboardHandler 
implements KeyboardListener, FocusListener, DeviceListener {
	
	private KeyboardAPI api;
	private final DeviceManager devMan;
	
	private boolean hasFocus;

    public DefaultKeyboardHandler(KeyboardAPI api) {
		if (api != null) {
			this.devMan = null;
			this.api = api;
			this.api.addKeyboardListener(this);
		}
		else {
			DeviceManager dm = null;
			try {
	            dm = InitialNaming.lookup(DeviceManager.NAME);
	            dm.addListener(this);
	        } 
			catch (NameNotFoundException ex) {
				BootLog.error("DeviceManager not found", ex);
	        }
			this.devMan = dm;
		}
	}
	
	private void registerKeyboardApi(Device device) {
		if (this.api == null) {
			try {
				this.api = device.getAPI(KeyboardAPI.class);
				this.api.addKeyboardListener(this);
			}
			catch (ApiNotFoundException ex) {
				BootLog.error("KeyboardAPI not found", ex);
			}
			this.devMan.removeListener(this);
		}
	}

	/**
	 * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyPressed(KeyboardEvent event) {
		if (hasFocus) {
			postEvent(event);
		}
	}

	/**
	 * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyReleased(KeyboardEvent event) {
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		if (api != null) {
			api.removeKeyboardListener(this);
		}
	}

	/**
	 * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
	 */
	public void deviceStarted(Device device) {
		if (device.implementsAPI(KeyboardAPI.class)) {
			registerKeyboardApi(device);
		}
	}

	/**
	 * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
	 */
	public void deviceStop(Device device) {
		/* Do nothing */
	}
	
	public void focusGained(FocusEvent event) {
		hasFocus = true;
	}

	public void focusLost(FocusEvent event) {
		hasFocus = false;
	}	
}

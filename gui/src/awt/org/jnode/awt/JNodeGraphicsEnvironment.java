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
 
package org.jnode.awt;

import gnu.java.security.action.GetPropertyAction;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.image.JNodeBufferedImageGraphics;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.naming.InitialNaming;

/**
 * JNode implementation of GraphicsEnvironment.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodeGraphicsEnvironment extends GraphicsEnvironment {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private JNodeFrameBufferDevice[] devices;
	private GraphicsDevice defaultDevice;

	/**
	 * @param image
	 * @see java.awt.GraphicsEnvironment#createGraphics(java.awt.image.BufferedImage)
	 * @return The graphics
	 */
	public Graphics2D createGraphics(BufferedImage image) {
		return new JNodeBufferedImageGraphics(image);
	}

	/**
	 * @see java.awt.GraphicsEnvironment#getAllFonts()
	 * @return All fonts
	 */
	public Font[] getAllFonts() {
		final FontManager fm = getFontManager();
		if (fm != null) {
			return fm.getAllFonts();
		} else {
			return new Font[0];
		}
	}

	/**
	 * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()
	 * @return All font family names
	 */
	public String[] getAvailableFontFamilyNames() {
		final Font[] fonts = getAllFonts();
		final HashSet names = new HashSet();
		for (int i = 0; i < fonts.length; i++) {
			names.add(fonts[i].getFamily());			
		}
		return (String[])names.toArray(new String[names.size()]);
	}

	/**
	 * @param l
	 * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames(java.util.Locale)
	 * @return All font family names
	 */
	public String[] getAvailableFontFamilyNames(Locale l) {
		final Font[] fonts = getAllFonts();
		final HashSet names = new HashSet();
		for (int i = 0; i < fonts.length; i++) {
			names.add(fonts[i].getFamily(l));			
		}
		return (String[])names.toArray(new String[names.size()]);
	}

	/**
	 * @see java.awt.GraphicsEnvironment#getDefaultScreenDevice()
	 * @return The default screen device
	 */
	public GraphicsDevice getDefaultScreenDevice() {
	    verifyCache();
		final String devId = (String)AccessController.doPrivileged(new GetPropertyAction("jnode.awt.device", "fb0"));
		if ((defaultDevice == null) || !devId.equals(defaultDevice.getIDstring())){
			final GraphicsDevice[] devs = getScreenDevices();
			for (int i = 0;(defaultDevice == null) && (i < devs.length); i++) {
				if (devs[i].getIDstring().equals(devId)) {
					defaultDevice = devs[i];
					log.debug("Using ScreenDevice " + defaultDevice.getIDstring());
				}
			}
		}
		return defaultDevice;
	}

	/**
	 * @param device
	 * @see java.awt.GraphicsEnvironment#getDefaultScreenDevice()
	 */
	public void setDefaultScreenDevice(GraphicsDevice device) {
		defaultDevice = device;
	}

	/**
	 * @see java.awt.GraphicsEnvironment#getScreenDevices()
	 * @return All screen devices
	 */
	public GraphicsDevice[] getScreenDevices() {
	    verifyCache();
		if (devices == null) {
			final Collection devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
			devices = new JNodeFrameBufferDevice[devs.size()];
			int idx = 0;
			for (Iterator i = devs.iterator(); i.hasNext(); idx++) {
				devices[idx] = new JNodeFrameBufferDevice((Device) i.next());
			}
		}
		return devices;
	}

	/**
	 * Gets the font manager, or null if not found.
	 * @return The font manager
	 */
	private FontManager getFontManager() {
		try {
			return (FontManager) InitialNaming.lookup(FontManager.NAME);
		} catch (NamingException ex) {
			return null;
		}
	}

	private final void verifyCache() {
	    if (devices != null) {
	        for (int i = 0; i < devices.length; i++) {
	            if (!devices[i].isActive()) {
	                // Reload the devices array
	                log.debug("Flushing AWT device cache");
	                devices = null;
	                defaultDevice = null;
	                return;
	            }
	        }
	    }
	}
}

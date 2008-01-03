/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.awt;

import gnu.java.security.action.GetPropertyAction;
import gnu.classpath.SystemProperties;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.font.DefaultTextPipe;
import org.jnode.awt.image.JNodeBufferedImageGraphics;
import org.jnode.awt.image.JNodeBufferedImageGraphics2D;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.naming.InitialNaming;
import sun.java2d.SurfaceData;
import sun.java2d.SunGraphics2D;

/**
 * JNode implementation of GraphicsEnvironment.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodeGraphicsEnvironment extends GraphicsEnvironment {

	/** My logger */
	private static final Logger log = Logger.getLogger(JNodeGraphicsEnvironment.class);
	private JNodeFrameBufferDevice[] devices;
	private GraphicsDevice defaultDevice;

	/**
	 * @param image
	 * @see java.awt.GraphicsEnvironment#createGraphics(java.awt.image.BufferedImage)
	 * @return The graphics
	 */
	public Graphics2D createGraphics(BufferedImage image) {
		return SystemProperties.getProperty("gnu.javax.swing.noGraphics2D") == null ?
        new JNodeBufferedImageGraphics2D(image) : new JNodeBufferedImageGraphics(image);

        /*
        ..future transition to SunGraphics2D based buffered image graphics
        SurfaceData sd = SurfaceData.getPrimarySurfaceData(image);
        SunGraphics2D g = new SunGraphics2D(sd, Color.WHITE, Color.BLACK, null);
        g.drawLine(0,0,0,0);
        g.textpipe = new DefaultTextPipe();
        return g;
        */
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
		final HashSet<String> names = new HashSet<String>();
		for (Font f : fonts) {
			names.add(f.getFamily());			
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
		final HashSet<String> names = new HashSet<String>();
		for (Font f : fonts) {
			names.add(f.getFamily(l));			
		}
		return (String[])names.toArray(new String[names.size()]);
	}

	/**
	 * @see java.awt.GraphicsEnvironment#getDefaultScreenDevice()
	 * @return The default screen device
	 */
	public GraphicsDevice getDefaultScreenDevice() {
	    verifyCache();
		final String devId = (String)AccessController.doPrivileged(new GetPropertyAction("jnode.awt.device"));
        boolean reload = (defaultDevice == null);
        if ((devId != null) && (defaultDevice != null)) {
            if (!devId.equals(defaultDevice.getIDstring())) {
                reload = true;
            }
        }
        
		if (reload) {
			final GraphicsDevice[] devs = getScreenDevices();
            if (devId != null) {
                for (int i = 0; (defaultDevice == null) && (i < devs.length); i++) {
                    if (devs[i].getIDstring().equals(devId)) {
                        defaultDevice = devs[i];
                        log.debug("Using ScreenDevice "
                                + defaultDevice.getIDstring());
                    }
                }
            }
            if ((defaultDevice == null) && (devs.length > 0)) {
                defaultDevice = devs[0];
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
			final Collection<Device> devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
			devices = new JNodeFrameBufferDevice[devs.size()];
			int idx = 0;
			for (Device dev : devs) {
				devices[idx++] = new JNodeFrameBufferDevice(dev);
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
			return InitialNaming.lookup(FontManager.NAME);
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

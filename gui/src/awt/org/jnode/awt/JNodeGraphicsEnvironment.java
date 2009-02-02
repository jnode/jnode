/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import gnu.classpath.SystemProperties;
import gnu.java.security.action.GetPropertyAction;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.image.JNodeBufferedImageGraphics2D;
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

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(JNodeGraphicsEnvironment.class);
    private JNodeFrameBufferDevice[] devices;
    private GraphicsDevice defaultDevice;

    /**
     * @param image the target image
     * @return The graphics
     * @see java.awt.GraphicsEnvironment#createGraphics(java.awt.image.BufferedImage)
     */
    public Graphics2D createGraphics(BufferedImage image) {
        return SystemProperties.getProperty("gnu.javax.swing.noGraphics2D") == null ?
            new JNodeBufferedImageGraphics2D(image) : GraphicsFactory.getInstance().createGraphics(image);

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
     * @return All fonts
     * @see java.awt.GraphicsEnvironment#getAllFonts()
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
     * @return All font family names
     * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()
     */
    public String[] getAvailableFontFamilyNames() {
        final Font[] fonts = getAllFonts();
        final HashSet<String> names = new HashSet<String>();
        for (Font f : fonts) {
            names.add(f.getFamily());
        }
        return (String[]) names.toArray(new String[names.size()]);
    }

    /**
     * @param l
     * @return All font family names
     * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames(java.util.Locale)
     */
    public String[] getAvailableFontFamilyNames(Locale l) {
        final Font[] fonts = getAllFonts();
        final HashSet<String> names = new HashSet<String>();
        for (Font f : fonts) {
            names.add(f.getFamily(l));
        }
        return (String[]) names.toArray(new String[names.size()]);
    }

    /**
     * @return The default screen device
     * @see java.awt.GraphicsEnvironment#getDefaultScreenDevice()
     */
    public GraphicsDevice getDefaultScreenDevice() {
        verifyCache();
        final String devId = (String) AccessController.doPrivileged(new GetPropertyAction("jnode.awt.device"));
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
                        log.debug("Using ScreenDevice " + defaultDevice.getIDstring());
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
     * @return All screen devices
     * @see java.awt.GraphicsEnvironment#getScreenDevices()
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
     *
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

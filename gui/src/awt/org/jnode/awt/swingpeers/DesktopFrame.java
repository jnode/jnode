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
 
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DesktopFrame extends JFrame implements JNodeAwtContext {
    private static final Color DESKTOP_BACKGROUND_COLOR = new Color(70, 130, 180);
	private final JDesktopPane desktop;
	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Initialize this instance.
	 *
	 */
	public DesktopFrame(Dimension screenSize) {
		super("");
		setSize(screenSize);
        desktop = new JDesktopPane();
        desktop.setBackground(DESKTOP_BACKGROUND_COLOR);
        getContentPane().add(desktop);
	}

    /**
	 * @return Returns the desktop.
	 */
	public final JDesktopPane getDesktop() {
		return desktop;
	}
	
	public final Container getAwtRoot() {
		return getContentPane();
	}

	/**
	 * @see javax.swing.JFrame#frameInit()
	 */
	protected void frameInit() {
		super.setLayout(new BorderLayout());
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		getRootPane(); // will do set/create
	}
}

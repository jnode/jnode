/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DesktopFrame extends JFrame {

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
        getContentPane().add(desktop);
	}
	
	/**
	 * @return Returns the desktop.
	 */
	final JDesktopPane getDesktop() {
		return desktop;
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

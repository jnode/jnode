/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DesktopFrame extends JFrame {

	private final JDesktopPane desktop;
	
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
}

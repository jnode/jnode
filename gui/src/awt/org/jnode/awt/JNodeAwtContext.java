/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.Container;

import javax.swing.JDesktopPane;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface JNodeAwtContext {
	
	/**
	 * Gets the desktop pane that holds all the Frames.
	 * @return
	 */
	public JDesktopPane getDesktop();
	
	/**
	 * Gets the root container of the screen, that holds the desktop.
	 * @return
	 */
	public Container getAwtRoot();
}

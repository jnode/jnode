/*
 * $Id$
 */
package org.jnode.desktop;

import java.awt.GridLayout;

import javax.swing.JPanel;

import org.jnode.plugin.ExtensionPoint;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ControlBar extends JPanel {

	private final ApplicationBar appsBar;
	private final WindowBar windowBar = new WindowBar(); 
	
	public ControlBar(ExtensionPoint appsExtensionPoint) {
		this.appsBar = new ApplicationBar(appsExtensionPoint);
		setLayout(new GridLayout(2, 1));
		add(appsBar);
		add(windowBar);
	}
	
	public ApplicationBar getApplicationBar() {
		return appsBar;
	}
	
	public WindowBar getWindowBar() {
		return windowBar;
	}
}

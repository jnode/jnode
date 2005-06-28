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
 
package org.jnode.desktop;

import java.awt.Color;
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
        setBackground(Color.BLUE);
	}
	
	public ApplicationBar getApplicationBar() {
		return appsBar;
	}
	
	public WindowBar getWindowBar() {
		return windowBar;
	}
}

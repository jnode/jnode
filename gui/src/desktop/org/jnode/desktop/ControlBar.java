/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
        setLayout(new BorderLayout());
        windowBar.setMinimumSize(new Dimension(200, 10));
        add(appsBar, BorderLayout.CENTER);
        add(windowBar, BorderLayout.EAST);
        setBackground(new Color(130, 255, 180));
    }

    public ApplicationBar getApplicationBar() {
        return appsBar;
    }

    public WindowBar getWindowBar() {
        return windowBar;
    }
}

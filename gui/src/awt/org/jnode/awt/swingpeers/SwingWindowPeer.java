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

import javax.swing.JInternalFrame;
import java.awt.AWTEvent;
import java.awt.Window;
import java.awt.peer.WindowPeer;

/**
 * AWT window peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente Sántha
 */

class SwingWindowPeer extends SwingContainerPeer implements WindowPeer {

    public SwingWindowPeer(SwingToolkit toolkit, Window window) {
        super(toolkit, window, new JInternalFrame());
        SwingToolkit.copyAwtProperties(window, jComponent);
    }
    public SwingWindowPeer(SwingToolkit toolkit, Window window, JInternalFrame jComponent) {
        super(toolkit, window, jComponent);
    }

    public void handleEvent(AWTEvent e) {
    }

    public void dispose() {
        ((JInternalFrame)jComponent).dispose();
		((SwingToolkit)toolkit).onDisposeFrame();
    }

    public void toBack() {
        ((JInternalFrame)jComponent).toBack();
    }

    public void toFront() {
        ((JInternalFrame)jComponent).toFront();
    }
}

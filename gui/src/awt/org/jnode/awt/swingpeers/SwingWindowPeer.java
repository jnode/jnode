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

import java.awt.Graphics;
import java.awt.Window;
import java.awt.peer.WindowPeer;

/**
 * AWT window peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente S\u00e1ntha
 */

final class SwingWindowPeer extends SwingBaseWindowPeer<Window, SwingWindow>
        implements WindowPeer {

    public SwingWindowPeer(SwingToolkit toolkit, Window window) {
        super(toolkit, window, new SwingWindow(window));
        SwingToolkit.copyAwtProperties(window, jComponent);
        jComponent.getContentPane().setLayout(new SwingContainerLayout(window, this));
    }
}

final class SwingWindow extends SwingBaseWindow<Window> {
    
    public SwingWindow(Window awtComponent) {
        super(awtComponent);
    }
    
    /**
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        SwingToolkit.paintLightWeightChildren(awtComponent, g, 0, 0);
    }
}


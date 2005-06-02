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
import java.awt.Window;
import java.awt.peer.WindowPeer;

import javax.swing.JInternalFrame;

/**
 * AWT window peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente S\u00e1ntha
 */

final class SwingWindowPeer extends SwingBaseWindowPeer<Window, SwingWindow>
        implements WindowPeer {

    public SwingWindowPeer(SwingToolkit toolkit, Window window) {
        super(toolkit, window, new SwingWindow(window));
        SwingToolkit.copyAwtProperties(window, jComponent);
    }
}

final class SwingWindow extends JInternalFrame implements ISwingPeer<Window> {
    
    private final Window awtComponent;
    
    public SwingWindow(Window awtComponent) {
        this.awtComponent = awtComponent;
    }
    
    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Window getAWTComponent() {
        return awtComponent;
    }
    
    /**
     * Pass an event onto the AWT component.
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(event);
    }
    
    /**
     * Process an event within this swingpeer
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }
}


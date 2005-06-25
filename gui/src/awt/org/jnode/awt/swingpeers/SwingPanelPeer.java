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
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.peer.PanelPeer;

import javax.swing.JPanel;

/**
 * AWT panel peer implemented as a {@link javax.swing.JPanel}.
 * @author Levente S\u00e1ntha
 */

final class SwingPanelPeer extends SwingContainerPeer<Panel, SwingPanel>
        implements PanelPeer, ISwingContainerPeer {

	//
	// Construction
	//

	public SwingPanelPeer(SwingToolkit toolkit, Panel panel) {
		super(toolkit, panel, new SwingPanel(panel));
		final SwingPanel jPanel = (SwingPanel) jComponent;
		SwingToolkit.add(panel, jPanel);
		SwingToolkit.copyAwtProperties(panel, jPanel);
        jComponent.setLayout(new SwingContainerLayout(panel, this));
	}

}

final class SwingPanel extends JPanel implements ISwingPeer<Panel> {
    private final Panel awtComponent;

    public SwingPanel(Panel awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Panel getAWTComponent() {
        return awtComponent;
    }

    /**
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        SwingToolkit.paintLightWeightChildren(awtComponent, g, 0, 0);
    }
    
    /**
     * Pass an event onto the AWT component.
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event, awtComponent));
    }
    
    /**
     * Process an event within this swingpeer
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }
}

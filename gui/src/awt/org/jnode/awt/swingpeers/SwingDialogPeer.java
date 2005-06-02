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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.peer.DialogPeer;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente S\u00e1ntha
 */

final class SwingDialogPeer extends SwingBaseWindowPeer<Dialog, SwingDialog>
        implements DialogPeer, ISwingContainerPeer {

	public SwingDialogPeer(SwingToolkit toolkit, Dialog dialog) {
        super(toolkit, dialog, new SwingDialog(dialog));
		jComponent.setTitle(dialog.getTitle());
        jComponent.getContentPane().setLayout(new SwingContainerLayout(this));
	}

	/**
	 * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		jComponent.getContentPane().add(peer);
	}

	public void dispose() {
        jComponent.dispose();
		toolkit.onDisposeFrame();
	}

    public void setResizable(boolean resizeable) {
        jComponent.setResizable(resizeable);
    }

    public void setTitle(String title) {
        jComponent.setTitle(title);
    }
}

final class SwingDialog extends JInternalFrame implements ISwingPeer<Dialog> {
    
    private final Dialog awtComponent;
    
    public SwingDialog(Dialog awtComponent) {
        this.awtComponent = awtComponent;
    }
    
    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Dialog getAWTComponent() {
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


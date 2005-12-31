/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ButtonPeer;

import javax.swing.JButton;

/**
 * AWT button peer implemented as a {@link javax.swing.JButton}.
 */

final class SwingButtonPeer extends SwingComponentPeer<Button, SwingButton> implements ButtonPeer {

    /**
     * Initialize this instance.
     */
	public SwingButtonPeer(SwingToolkit toolkit, Button button) {
        super(toolkit, button, new SwingButton(button));
		SwingToolkit.add(button, peerComponent);
		SwingToolkit.copyAwtProperties(button, peerComponent);
        peerComponent.setText(button.getLabel());
	}

    public void setLabel(String label) {
        peerComponent.setText(label);
    }
}


final class SwingButton extends JButton implements ISwingPeer<Button>,
        ActionListener {
    private final Button awtComponent;

    /**
     * Initialize this instance.
     * @param awtComponent
     */
    public SwingButton(Button awtComponent) {
        this.awtComponent = awtComponent;
        addActionListener(this);
    }
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {        
        awtComponent.dispatchEvent(new ActionEvent(awtComponent,
                ActionEvent.ACTION_PERFORMED, awtComponent.getActionCommand()));
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Button getAWTComponent() {
        return awtComponent;
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

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#validatePeerOnly()
     */
    public final void validatePeerOnly() {
        super.validate();
    }    
}

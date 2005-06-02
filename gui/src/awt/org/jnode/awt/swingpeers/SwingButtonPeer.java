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

import org.jnode.awt.swingpeers.event.ActionListenerDelegate;

import javax.swing.JButton;
import java.awt.Button;
import java.awt.Component;
import java.awt.peer.ButtonPeer;

/**
 * AWT button peer implemented as a {@link javax.swing.JButton}.
 */

final class SwingButtonPeer extends SwingComponentPeer<Button, SwingButton> implements ButtonPeer {

	// Construction
	//

	public SwingButtonPeer(SwingToolkit toolkit, Button button) {
        super(toolkit, button, new SwingButton(button));
        final JButton jButton = (JButton)jComponent;
		SwingToolkit.add(button, jButton);
		SwingToolkit.copyAwtProperties(button, jButton);
		jButton.setText(button.getLabel());
		jButton.addActionListener(new ActionListenerDelegate(button));
	}

    public void setLabel(String label) {
        ((JButton)jComponent).setText(label);
    }
}


final class SwingButton extends JButton implements ISwingPeer {
    private final Button awtComponent;

    public SwingButton(Button awtComponent) {
        this.awtComponent = awtComponent;
    }
    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Component getAWTComponent() {
        return awtComponent;
    }       
}

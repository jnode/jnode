/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Button;
import java.awt.Component;
import java.awt.peer.ButtonPeer;

import javax.swing.JButton;

import org.jnode.awt.swingpeers.event.ActionListenerDelegate;
import org.jnode.awt.swingpeers.event.MouseListenerDelegate;
import org.jnode.awt.swingpeers.event.MouseMotionListenerDelegate;

/**
 * AWT button peer implemented as a {@link javax.swing.JButton}.
 */

class SwingButtonPeer extends SwingComponentPeer implements ButtonPeer {

	// Construction
	//

	public SwingButtonPeer(SwingToolkit toolkit, Button button) {
        super(toolkit, button, new SwingButton(button));
        final JButton jButton = (JButton)jComponent;
		SwingToolkit.add(button, jButton);
		SwingToolkit.copyAwtProperties(button, jButton);
		jButton.setText(button.getLabel());
		jButton.addActionListener(new ActionListenerDelegate(button));
		jButton.addMouseListener(new MouseListenerDelegate(button));
		jButton.addMouseMotionListener(new MouseMotionListenerDelegate(button));

	}

    public void setLabel(String label) {
        ((JButton)jComponent).setText(label);
    }

    private static class SwingButton extends JButton implements SwingPeer {
    	private final Button awtComponent;

    	public SwingButton(Button awtComponent) {
    		this.awtComponent = awtComponent;
    	}
    	/**
    	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
    	 */
    	public Component getAWTComponent() {
    		return awtComponent;
    	}    	
    }
}
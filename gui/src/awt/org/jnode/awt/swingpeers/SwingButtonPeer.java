/*
 * $Id$
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
	}

    public void setLabel(String label) {
        ((JButton)jComponent).setText(label);
    }

    private static class SwingButton extends JButton implements ISwingPeer {
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
}
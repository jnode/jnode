/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JComponent;
import java.awt.Component;
import java.awt.peer.LightweightPeer;

/**
 * AWT lightweight component peers that does nothing.
 */

final class SwingLightweightPeer extends SwingComponentPeer implements
		LightweightPeer, ISwingPeer {
    
	public SwingLightweightPeer(SwingToolkit toolkit, Component component) {
		super(toolkit, component, new JLightweightComponent());
	}

	/**
	 * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return (Component) component;
	}

	private static class JLightweightComponent extends JComponent {
		
	}
}
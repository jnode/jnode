/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.peer.PanelPeer;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * AWT panel peer implemented as a {@link javax.swing.JPanel}.
 */

final class SwingPanelPeer extends SwingComponentPeer implements PanelPeer,
		SwingContainerPeer {

	//
	// Construction
	//

	public SwingPanelPeer(SwingToolkit toolkit, Panel panel) {
		super(toolkit, panel, new SwingPanel(panel));
		final SwingPanel jPanel = (SwingPanel) jComponent;
		SwingToolkit.add(panel, jPanel);
		SwingToolkit.copyAwtProperties(panel, jPanel);
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		((JPanel) jComponent).add(peer);
	}

	public void beginLayout() {
	}

	//
	// ContainerPeer
	//

	public void beginValidate() {
	}

	public void endLayout() {
	}

	public void endValidate() {
	}

	public boolean isPaintPending() {
		return false;
	}

	public Insets getInsets() {
		return ((JPanel) jComponent).getInsets();
	}

	public Insets insets() {
		return getInsets();
	}

	private static class SwingPanel extends JPanel implements SwingPeer {
		private final Panel awtComponent;

		public SwingPanel(Panel awtComponent) {
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
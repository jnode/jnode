/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.peer.PanelPeer;

/**
 * AWT panel peer implemented as a {@link javax.swing.JPanel}.
 * @author Levente Sántha
 */

final class SwingPanelPeer extends SwingContainerPeer implements PanelPeer, ISwingContainerPeer {

	//
	// Construction
	//

	public SwingPanelPeer(SwingToolkit toolkit, Panel panel) {
		super(toolkit, panel, new SwingPanel(panel));
		final SwingPanel jPanel = (SwingPanel) jComponent;
        jPanel.swingPeer = this;
		SwingToolkit.add(panel, jPanel);
		SwingToolkit.copyAwtProperties(panel, jPanel);
	}

	private static class SwingPanel extends JPanel implements ISwingPeer {
		private final Panel awtComponent;
        private SwingPanelPeer swingPeer;

		public SwingPanel(Panel awtComponent) {
			this.awtComponent = awtComponent;
		}

		/**
		 * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
		 */
		public Component getAWTComponent() {
			return awtComponent;
		}

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            awtComponent.update(g);
        }

		/**
		 * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
		 */
		protected void paintChildren(Graphics g) {
			super.paintChildren(g);
			SwingToolkit.paintLightWeightChildren(awtComponent, g, 0, 0);
		}
	}
}
/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JComponent;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.peer.CanvasPeer;

/**
 * AWT canvas peer implemented as a {@link javax.swing.JComponent}.
 */

class SwingCanvasPeer extends SwingComponentPeer implements CanvasPeer {

	public SwingCanvasPeer(SwingToolkit toolkit, Canvas canvas) {
        super(toolkit, canvas, new SwingCanvas(canvas));
		SwingToolkit.add(canvas, jComponent);
		SwingToolkit.copyAwtProperties(canvas, jComponent);
	}

	private static class SwingCanvas extends JComponent implements ISwingPeer {
		private final Canvas awtComponent;

		public SwingCanvas(Canvas awtComponent) {
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
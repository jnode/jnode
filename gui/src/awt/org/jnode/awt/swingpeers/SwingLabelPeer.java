/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Label;
import java.awt.peer.LabelPeer;

/**
 * AWT label peer implemented as a {@link javax.swing.JLabel}.
 */

class SwingLabelPeer extends SwingComponentPeer implements LabelPeer {

	//
	// Construction
	//

	public SwingLabelPeer(SwingToolkit toolkit, Label label) {
		super(toolkit, label, new SwingLabel(label));
		final JLabel jLabel = (JLabel) jComponent;
		SwingToolkit.add(label, jLabel);
		SwingToolkit.copyAwtProperties(label, jLabel);
		setText(label.getText());
	}

	public void setText(String text) {
		((JLabel) jComponent).setText(text);
	}

	public void setAlignment(int alignment) {
		//TODO implement it
	}

	private static class SwingLabel extends JLabel implements ISwingPeer {
		private final Label awtComponent;

		public SwingLabel(Label awtComponent) {
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
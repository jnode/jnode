/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.PaintEvent;
import java.awt.peer.LabelPeer;

import javax.swing.JLabel;

/**
 * AWT label peer implemented as a {@link javax.swing.JLabel}.
 */

class SwingLabelPeer extends SwingComponentPeer implements LabelPeer, SwingPeer {

	private final Label label;

	//
	// Construction
	//

	public SwingLabelPeer(SwingToolkit toolkit, Label label) {
        super(toolkit, label);
        JLabel jLabel = new JLabel();
		this.label = label;
        jComponent = jLabel;
		SwingToolkit.add(label, jLabel);
		SwingToolkit.copyAwtProperties(label, jLabel);
		setText(label.getText());
	}

    public void setText(String text) {
        ((JLabel)jComponent).setText(text);
    }

  	public void setAlignment(int alignment) {
        //TODO implement it
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return label;
	}


}
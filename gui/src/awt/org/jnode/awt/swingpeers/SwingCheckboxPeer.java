/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.PaintEvent;
import java.awt.peer.CheckboxPeer;

import javax.swing.JCheckBox;

/**
 * AWT checkbox peer implemented as a {@link javax.swing.JCheckBox}.
 */
class SwingCheckboxPeer extends SwingComponentPeer implements CheckboxPeer, SwingPeer {

	private final Checkbox checkBox;

	//
	// Construction
	//

	public SwingCheckboxPeer(SwingToolkit toolkit, Checkbox checkBox) {
        super(toolkit, checkBox);
        JCheckBox jcb = new JCheckBox();
		this.checkBox = checkBox;
        jComponent = jcb;
		SwingToolkit.add(checkBox, jcb);
		SwingToolkit.copyAwtProperties(checkBox, jcb);
		jcb.setText(checkBox.getLabel());
		setState(checkBox.getState());

	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return checkBox;
	}

	public void setCheckboxGroup(CheckboxGroup g) {
	}

	public void setState(boolean state) {
		((JCheckBox)jComponent).setSelected(state);
	}

    public void setLabel(String label) {
        ((JCheckBox)jComponent).setText(label);
    }
}

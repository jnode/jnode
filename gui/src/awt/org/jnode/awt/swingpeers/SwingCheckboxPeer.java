/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.peer.CheckboxPeer;

import javax.swing.JCheckBox;

/**
 * AWT checkbox peer implemented as a {@link javax.swing.JCheckBox}.
 */
class SwingCheckboxPeer extends SwingComponentPeer implements CheckboxPeer {

	//
	// Construction
	//

	public SwingCheckboxPeer(SwingToolkit toolkit, Checkbox checkBox) {
		super(toolkit, checkBox, new SwingCheckBox(checkBox));
		final JCheckBox jcb = (JCheckBox) jComponent;
		SwingToolkit.add(checkBox, jcb);
		SwingToolkit.copyAwtProperties(checkBox, jcb);
		jcb.setText(checkBox.getLabel());
		setState(checkBox.getState());

	}

	public void setCheckboxGroup(CheckboxGroup g) {
	}

	public void setState(boolean state) {
		((JCheckBox) jComponent).setSelected(state);
	}

	public void setLabel(String label) {
		((JCheckBox) jComponent).setText(label);
	}

	private static class SwingCheckBox extends JCheckBox implements ISwingPeer {
		private final Checkbox awtComponent;

		public SwingCheckBox(Checkbox awtComponent) {
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

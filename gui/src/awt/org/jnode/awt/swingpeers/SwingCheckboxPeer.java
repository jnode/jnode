/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt.swingpeers;

import javax.swing.JCheckBox;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.peer.CheckboxPeer;

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

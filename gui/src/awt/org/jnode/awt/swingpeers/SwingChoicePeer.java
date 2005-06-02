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

import java.awt.Choice;
import java.awt.peer.ChoicePeer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * AWT choice peer implemented as a {@link javax.swing.JButton}.
 * @author Levente S\u00e1ntha
 */

final class SwingChoicePeer extends SwingComponentPeer<Choice, SwingChoice> implements ChoicePeer {

	//
	// Construction
	//

	public SwingChoicePeer(SwingToolkit toolkit, Choice choice) {
		super(toolkit, choice, new SwingChoice(choice));
		final JComboBox combo = (JComboBox) jComponent;
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        final int cnt = choice.getItemCount();
        for (int i = 0; i < cnt; i++) {
			model.addElement(choice.getItem(i));
		}
		combo.setModel(model);
		SwingToolkit.add(choice, combo);
		SwingToolkit.copyAwtProperties(choice, combo);
	}

	//
	// ChoicePeer
	//

	public void remove(int index) {
		model().removeElementAt(index);
	}

	public void removeAll() {
		model().removeAllElements();
	}

	public void add(String item, int index) {
		model().insertElementAt(item, index);
	}

	private DefaultComboBoxModel model() {
		return ((DefaultComboBoxModel) ((JComboBox) jComponent).getModel());
	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	public void select(int index) {
		((JComboBox) jComponent).setSelectedIndex(index);
	}

}

final class SwingChoice extends JComboBox implements ISwingPeer<Choice> {
    private final Choice awtComponent;

    public SwingChoice(Choice awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Choice getAWTComponent() {
        return awtComponent;
    }
}


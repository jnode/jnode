/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.PaintEvent;
import java.awt.peer.ChoicePeer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 * AWT choice peer implemented as a {@link javax.swing.JButton}.
 */

class SwingChoicePeer extends SwingComponentPeer implements ChoicePeer, SwingPeer {

	private final Choice choice;

	//
	// Construction
	//

	public SwingChoicePeer(SwingToolkit toolkit, Choice choice) {
        super(toolkit, choice);
		this.choice = choice;
        JComboBox combo = new JComboBox();
        jComponent = combo;
        combo.setModel(new DefaultComboBoxModel());
		SwingToolkit.add(choice, combo);
		SwingToolkit.copyAwtProperties(choice, combo);
		final int cnt = choice.getItemCount();
		for (int i = 0; i < cnt; i++) {
			addItem(choice.getItem(i), i);
		}
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
        return ((DefaultComboBoxModel)((JComboBox)jComponent).getModel());
    }

    // Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return choice;
	}
	public void select(int index) {
        ((JComboBox)jComponent).setSelectedIndex(index);
	}
}
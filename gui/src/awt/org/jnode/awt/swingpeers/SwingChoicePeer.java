/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Choice;
import java.awt.Component;
import java.awt.peer.ChoicePeer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * AWT choice peer implemented as a {@link javax.swing.JButton}.
 */

final class SwingChoicePeer extends SwingComponentPeer implements ChoicePeer {

	//
	// Construction
	//

	public SwingChoicePeer(SwingToolkit toolkit, Choice choice) {
		super(toolkit, choice, new SwingChoice(choice));
		final JComboBox combo = (JComboBox) jComponent;
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
		return ((DefaultComboBoxModel) ((JComboBox) jComponent).getModel());
	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	public void select(int index) {
		((JComboBox) jComponent).setSelectedIndex(index);
	}

	private static class SwingChoice extends JComboBox implements ISwingPeer {
		private final Choice awtComponent;

		public SwingChoice(Choice awtComponent) {
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
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

import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.peer.TextFieldPeer;

/**
 * AWT text field peer implemented as a {@link javax.swing.JTextField}.
 */

final class SwingTextFieldPeer extends SwingTextComponentPeer implements
		TextFieldPeer {

	//
	// Construction
	//

	public SwingTextFieldPeer(SwingToolkit toolkit, TextField textField) {
		super(toolkit, textField, new SwingTextField(textField));

		final SwingTextField jtf = (SwingTextField) jComponent;
		setText(textField.getText());
		jtf.setColumns(textField.getColumns());
		setEditable(textField.isEditable());
	}

	/**
	 * @see java.awt.peer.TextFieldPeer#getMinimumSize(int)
	 */
	public Dimension getMinimumSize(int len) {
		return ((JTextField)jComponent).getMinimumSize();
	}

	/**
	 * @see java.awt.peer.TextFieldPeer#getPreferredSize(int)
	 */
	public Dimension getPreferredSize(int len) {
		return ((JTextField)jComponent).getPreferredSize();
	}

	/**
	 * @see java.awt.peer.TextFieldPeer#minimumSize(int)
	 */
	public Dimension minimumSize(int len) {
		return getMinimumSize(len);
	}

	/**
	 * @see java.awt.peer.TextFieldPeer#preferredSize(int)
	 */
	public Dimension preferredSize(int len) {
		return getPreferredSize(len);
	}

	/**
	 * @see java.awt.peer.TextFieldPeer#setEchoChar(char)
	 */
	public void setEchoChar(char echo_char) {
		setEchoCharacter(echo_char);
	}

	/**
	 * @see java.awt.peer.TextFieldPeer#setEchoCharacter(char)
	 */
	public void setEchoCharacter(char echo_char) {
		// TODO Auto-generated method stub

	}

	private static class SwingTextField extends JTextField implements ISwingPeer {
		private final TextField awtComponent;

		public SwingTextField(TextField awtComponent) {
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

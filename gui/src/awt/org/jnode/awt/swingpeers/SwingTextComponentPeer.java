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

import javax.swing.text.JTextComponent;
import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextComponentPeer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingTextComponentPeer<awtT extends TextComponent, peerT extends JTextComponent>
        extends SwingComponentPeer<awtT, peerT> implements TextComponentPeer {

	public SwingTextComponentPeer(SwingToolkit toolkit,
			awtT textComponent, peerT peer) {
		super(toolkit, textComponent, peer);

		SwingToolkit.add(textComponent, jComponent);
		SwingToolkit.copyAwtProperties(textComponent, jComponent);
		setText(textComponent.getText());
		setEditable(textComponent.isEditable());
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#filterEvents(long)
	 */
	public long filterEvents(long filter) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getCaretPosition()
	 */
	public int getCaretPosition() {
		return jComponent.getCaretPosition();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getCharacterBounds(int)
	 */
	public Rectangle getCharacterBounds(int pos) {
		// TODO implement me
		return null;
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getIndexAtPoint(int, int)
	 */
	public int getIndexAtPoint(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getSelectionEnd()
	 */
	public int getSelectionEnd() {
		return jComponent.getSelectionEnd();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getSelectionStart()
	 */
	public int getSelectionStart() {
		return jComponent.getSelectionStart();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#getText()
	 */
	public String getText() {
		return jComponent.getText();
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#select(int, int)
	 */
	public void select(int start_pos, int end_pos) {
		jComponent.select(start_pos, end_pos);
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#setCaretPosition(int)
	 */
	public void setCaretPosition(int pos) {
		jComponent.setCaretPosition(pos);
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#setEditable(boolean)
	 */
	public void setEditable(boolean editable) {
		jComponent.setEditable(editable);
	}

	/**
	 * @see java.awt.peer.TextComponentPeer#setText(java.lang.String)
	 */
	public void setText(String text) {
		jComponent.setText(text);
	}

    public InputMethodRequests getInputMethodRequests() {
        return null;  //TODO implement it
    }
}

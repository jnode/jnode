/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.PaintEvent;
import java.awt.peer.TextFieldPeer;

import javax.swing.JTextField;

/**
 * AWT text field peer implemented as a {@link javax.swing.JTextField}.
 */

class SwingTextFieldPeer extends JTextField implements TextFieldPeer, SwingPeer {

	private final TextField textField;

	//
	// Construction
	//

	public SwingTextFieldPeer(TextField textField) {
		this.textField = textField;
		SwingToolkit.add(textField, this);
		SwingToolkit.copyAwtProperties(textField, this);
		setText(textField.getText());
		setColumns(textField.getColumns());
		setEditable(textField.isEditable());
	}

	public boolean canDetermineObscurity() {
		return false;
	}

	public void coalescePaintEvent(PaintEvent e) {
		System.err.println(e);
	}

	// Buffer

	public void createBuffers(int x, BufferCapabilities bufferCapabilities) {
	}

	public void destroyBuffers() {
	}

	// Misc

	public void dispose() {
	}

	public long filterEvents(long mask) {
		return 0;
	}

	public void flip(BufferCapabilities.FlipContents flipContents) {
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return textField;
	}

	public Image getBackBuffer() {
		return null;
	}

	public Rectangle getCharacterBounds(int i) {
		return null;
	}

	//
	// TextComponentPeer
	//

	public int getIndexAtPoint(int x, int y) {
		return 0;
	}

	public Dimension getMinimumSize(int columns) {
		return null;
	}

	public Dimension getPreferredSize(int columns) {
		return null;
	}

	//
	// ComponentPeer
	//

	// Events

	public void handleEvent(AWTEvent e) {
		//System.err.println(e);
	}

	public boolean handlesWheelScrolling() {
		return false;
	}

	// Obscurity

	public boolean isObscured() {
		return false;
	}

	public Dimension minimumSize(int cols) {
		return getMinimumSize(cols);
	}

	public Dimension preferredSize(int cols) {
		return getPreferredSize(cols);
	}

	// Focus

	public boolean requestFocus(Component lightweightChild, boolean temporary,
			boolean focusedWindowChangeAllowed, long time) {
		return true;
	}

	//
	// TextFieldPeer
	//

	public void setEchoChar(char echoChar) {
	}

	// Deprecated

	public void setEchoCharacter(char c) {
		setEchoChar(c);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private
	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
		// TODO Auto-generated method stub

	}

	// Cursor

	public void updateCursorImmediately() {
	}
}
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

/**
 * AWT choice peer implemented as a {@link javax.swing.JButton}.
 */

class SwingChoicePeer extends JButton implements ChoicePeer, SwingPeer {

	private final Choice choice;

	//
	// Construction
	//

	public SwingChoicePeer(Choice choice) {
		this.choice = choice;
		SwingToolkit.add(choice, this);
		SwingToolkit.copyAwtProperties(choice, this);
		final int cnt = choice.getItemCount();
		for (int i = 0; i < cnt; i++) {
			addItem(choice.getItem(i), i);
		}
	}

	//
	// ChoicePeer
	//

	public void add(String item, int index) {
	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
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

	public void flip(BufferCapabilities.FlipContents flipContents) {
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return choice;
	}

	public Image getBackBuffer() {
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

	// Focus

	public boolean requestFocus(Component lightweightChild, boolean temporary,
			boolean focusedWindowChangeAllowed, long time) {
		return true;
	}

	public void select(int index) {
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
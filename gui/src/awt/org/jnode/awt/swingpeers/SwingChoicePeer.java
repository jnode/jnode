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

class SwingChoicePeer extends JButton implements ChoicePeer {

	//
	// Construction
	//

	public SwingChoicePeer(Choice choice) {
		super();
		SwingFramePeer.add(choice, this);
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

	public void select(int index) {
	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	//
	// ComponentPeer
	//

	// Events

	public void handleEvent(AWTEvent e) {
		//System.err.println(e);
	}

	public void coalescePaintEvent(PaintEvent e) {
		System.err.println(e);
	}

	public boolean handlesWheelScrolling() {
		return false;
	}

	// Obscurity

	public boolean isObscured() {
		return false;
	}

	public boolean canDetermineObscurity() {
		return false;
	}

	// Focus

	public boolean requestFocus(Component lightweightChild, boolean temporary,
			boolean focusedWindowChangeAllowed, long time) {
		return true;
	}

	// Buffer

	public void createBuffers(int x, BufferCapabilities bufferCapabilities) {
	}

	public void destroyBuffers() {
	}

	public void flip(BufferCapabilities.FlipContents flipContents) {
	}

	public Image getBackBuffer() {
		return null;
	}

	// Cursor

	public void updateCursorImmediately() {
	}

	// Misc

	public void dispose() {
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private
	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
		// TODO Auto-generated method stub

	}
}
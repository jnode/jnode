/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.PaintEvent;
import java.awt.peer.LabelPeer;

import javax.swing.JLabel;

/**
 * AWT label peer implemented as a {@link javax.swing.JLabel}.
 */

class SwingLabelPeer extends JLabel implements LabelPeer, SwingPeer {

	private final Label label;

	//
	// Construction
	//

	public SwingLabelPeer(Label label) {
		this.label = label;
		SwingToolkit.add(label, this);
		SwingToolkit.copyAwtProperties(label, this);
		setText(label.getText());
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
		return label;
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

	//
	// LabelPeer
	//

	public void setAlignment(int alignment) {
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
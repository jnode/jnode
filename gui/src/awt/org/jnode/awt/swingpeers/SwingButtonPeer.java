/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Button;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.PaintEvent;
import java.awt.peer.ButtonPeer;

import javax.swing.JButton;

import org.jnode.awt.swingpeers.event.ActionListenerDelegate;
import org.jnode.awt.swingpeers.event.MouseListenerDelegate;
import org.jnode.awt.swingpeers.event.MouseMotionListenerDelegate;

/**
 * AWT button peer implemented as a {@link javax.swing.JButton}.
 */

class SwingButtonPeer extends JButton implements ButtonPeer, SwingPeer {
	private final Button button;

	// Construction
	//

	public SwingButtonPeer(Button button) {
		this.button = button;
		SwingToolkit.add(button, this);
		SwingToolkit.copyAwtProperties(button, this);
		setText(button.getLabel());
		addActionListener(new ActionListenerDelegate(button));
		addMouseListener(new MouseListenerDelegate(button));
		addMouseMotionListener(new MouseMotionListenerDelegate(button));
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
		return button;
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

	public boolean requestFocus(Component lightweightChild, boolean temporary,
			boolean focusedWindowChangeAllowed, long time) {
		return true;
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
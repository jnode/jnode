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

class SwingButtonPeer extends JButton implements ButtonPeer {

	//
	// Construction
	//

	public SwingButtonPeer(Button button) {
		SwingFramePeer.add(button, this);
		SwingToolkit.copyAwtProperties(button, this);
		setText(button.getLabel());
		addActionListener(new ActionListenerDelegate(button));
		addMouseListener(new MouseListenerDelegate(button));
		addMouseMotionListener(new MouseMotionListenerDelegate(button));
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
/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.PaintEvent;
import java.awt.peer.CheckboxPeer;

import javax.swing.JCheckBox;

/**
 * AWT checkbox peer implemented as a {@link javax.swing.JCheckBox}.
 */
class SwingCheckboxPeer extends JCheckBox implements CheckboxPeer, SwingPeer {

	private final Checkbox checkBox;

	//
	// Construction
	//

	public SwingCheckboxPeer(Checkbox checkBox) {
		this.checkBox = checkBox;
		SwingToolkit.add(checkBox, this);
		SwingToolkit.copyAwtProperties(checkBox, this);
		setText(checkBox.getLabel());
		setState(checkBox.getState());
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
		return checkBox;
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

	public void setCheckboxGroup(CheckboxGroup g) {
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private
	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
		// TODO Auto-generated method stub

	}

	//
	// CheckboxPeer
	//

	public void setState(boolean state) {
		setSelected(state);
	}

	// Cursor

	public void updateCursorImmediately() {
	}
}
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
class SwingCheckboxPeer extends JCheckBox implements CheckboxPeer {

	//
	// Construction
	//

	public SwingCheckboxPeer(Checkbox checkBox) {
		SwingToolkit.add(checkBox, this);
		SwingToolkit.copyAwtProperties(checkBox, this);
		setText(checkBox.getLabel());
		setState(checkBox.getState());
	}

	//
	// CheckboxPeer
	//

	public void setState(boolean state) {
		setSelected(state);
	}

	public void setCheckboxGroup(CheckboxGroup g) {
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
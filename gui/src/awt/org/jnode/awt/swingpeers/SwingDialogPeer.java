/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.PaintEvent;
import java.awt.peer.DialogPeer;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 */

class SwingDialogPeer extends JInternalFrame implements DialogPeer,
		ISwingContainerPeer {

	//
	// Construction
	//

	public SwingDialogPeer(Dialog dialog) {
		SwingToolkit.copyAwtProperties(dialog, this);
		setTitle(dialog.getTitle());
		getContentPane().setLayout(null);
	}

	/**
	 * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		getContentPane().add(peer);
	}

	public void beginLayout() {
	}

	//
	// ContainerPeer
	//

	public void beginValidate() {
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

	public void endLayout() {
	}

	public void endValidate() {
	}

	public void flip(BufferCapabilities.FlipContents flipContents) {
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

	//
	// WindowPeer
	//

	public int handleFocusTraversalEvent(KeyEvent e) {
		return -1;
	}

	public boolean handlesWheelScrolling() {
		return false;
	}

	// Obscurity

	public boolean isObscured() {
		return false;
	}

	public boolean isPaintPending() {
		return false;
	}

	// Focus

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
/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.PaintEvent;
import java.awt.peer.PanelPeer;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * AWT panel peer implemented as a {@link javax.swing.JPanel}.
 */

class SwingPanelPeer extends JPanel implements PanelPeer, SwingContainerPeer {
	//
	// Construction
	//

	public SwingPanelPeer(Panel panel) {
		super();
		SwingToolkit.add(panel, this);
		SwingToolkit.copyAwtProperties(panel, this);
		setLayout(null);
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		add(peer);
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
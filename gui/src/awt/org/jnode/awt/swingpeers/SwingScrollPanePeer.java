/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.Adjustable;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.event.PaintEvent;
import java.awt.peer.ScrollPanePeer;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * AWT scroll pane peer implemented as a {@link javax.swing.JScrollPane}.
 */

class SwingScrollPanePeer extends JScrollPane implements ScrollPanePeer,
		SwingContainerPeer, SwingPeer {
	private final ScrollPane scrollPane;

	//
	// Construction
	//

	public SwingScrollPanePeer(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		SwingToolkit.add(scrollPane, this);
		SwingToolkit.copyAwtProperties(scrollPane, this);
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

	public void childResized(int w, int h) {
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

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return scrollPane;
	}

	public Image getBackBuffer() {
		return null;
	}

	//
	// ScrollPanePeer
	//

	public int getHScrollbarHeight() {
		return 0;
	}

	public int getVScrollbarWidth() {
		return 0;
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

	public void setScrollPosition(int x, int y) {
	}

	public void setUnitIncrement(Adjustable adj, int u) {
	}

	public void setValue(Adjustable adj, int v) {
	}

	// Cursor

	public void updateCursorImmediately() {
	}
}
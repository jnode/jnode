/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.event.KeyEvent;
import java.awt.event.PaintEvent;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * AWT frame peer implemented as a {@link javax.swing.JInternalFrame}.
 */

class SwingFramePeer extends JInternalFrame implements FramePeer,
		SwingContainerPeer {

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private final SwingToolkit toolkit;

	//
	// Construction
	//

	public SwingFramePeer(SwingToolkit toolkit, JDesktopPane desktopPane,
			Frame frame) {
		super();
		this.toolkit = toolkit;
		desktopPane.add(this);

		SwingToolkit.copyAwtProperties(frame, this);
		getContentPane().setLayout(null);
		setLocation(frame.getLocation());
		setSize(frame.getSize());
		setResizable(frame.isResizable());
		setIconifiable(true);
		setMaximizable(true);
		setClosable(true);
		try {
			setIcon(frame.getState() == Frame.ICONIFIED);
		} catch (PropertyVetoException x) {
		}
		setState(frame.getState());
		setTitle(frame.getTitle());
		setIconImage(frame.getIconImage());
		setMenuBar(frame.getMenuBar());
	}

	//
	// Static operations
	//

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
		toolkit.onDisposeFrame();
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

	public int getState() {
		return -1;
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

	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
		// TODO Auto-generated method stub

	}

	//
	// FramePeer
	//

	public void setIconImage(Image im) {
	}

	public void setMaximizedBounds(java.awt.Rectangle bounds) {
	}

	public void setMenuBar(MenuBar mb) {
	}

	public void setState(int state) {
		if (state == Frame.ICONIFIED) {
		} else // state == Frame.NORMAL
		{
		}
	}

	// Cursor

	public void updateCursorImmediately() {
	}
}
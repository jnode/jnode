/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.List;
import java.awt.event.PaintEvent;
import java.awt.peer.ListPeer;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * AWT list peer implemented as a {@link javax.swing.JList}.
 */

class SwingListPeer extends JList implements ListPeer, SwingPeer {

	private final List list;

	//
	// Construction
	//

	public SwingListPeer(final List list) {
		this.list = list;
		SwingToolkit.add(list, this);
		SwingToolkit.copyAwtProperties(list, this);
		final ListModel model = new AbstractListModel() {
			public Object getElementAt(int idx) {
				return list.getItem(idx);
			}

			public int getSize() {
				return list.getItemCount();
			}
		};
	}

	public void add(String item, int index) {
	}

	// Deprecated

	public void addItem(String item, int index) {
		add(item, index);
	}

	public boolean canDetermineObscurity() {
		return false;
	}

	public void clear() {
		removeAll();
	}

	public void coalescePaintEvent(PaintEvent e) {
		System.err.println(e);
	}

	// Buffer

	public void createBuffers(int x, BufferCapabilities bufferCapabilities) {
	}

	public void delItems(int start, int end) {
	}

	public void deselect(int index) {
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
		return list;
	}

	public Image getBackBuffer() {
		return null;
	}

	public Dimension getMinimumSize(int rows) {
		return getMinimumSize();
	}

	public Dimension getPreferredSize(int rows) {
		return getPreferredSize();
	}

	//
	// ListPeer
	//

	public int[] getSelectedIndexes() {
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

	public void makeVisible(int index) {
	}

	public Dimension minimumSize(int rows) {
		return getMinimumSize(rows);
	}

	public Dimension preferredSize(int rows) {
		return getPreferredSize(rows);
	}

	// Focus

	public boolean requestFocus(Component lightweightChild, boolean temporary,
			boolean focusedWindowChangeAllowed, long time) {
		return true;
	}

	public void select(int index) {
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private
	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
		// TODO Auto-generated method stub

	}

	public void setMultipleMode(boolean b) {
	}

	public void setMultipleSelections(boolean v) {
		setMultipleMode(v);
	}

	// Cursor

	public void updateCursorImmediately() {
	}
}
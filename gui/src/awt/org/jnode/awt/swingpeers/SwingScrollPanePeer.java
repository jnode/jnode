/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JScrollPane;
import java.awt.Adjustable;
import java.awt.Component;
import java.awt.ScrollPane;
import java.awt.peer.ScrollPanePeer;

/**
 * AWT scroll pane peer implemented as a {@link javax.swing.JScrollPane}.
 */

final class SwingScrollPanePeer extends SwingContainerPeer implements
		ScrollPanePeer {

	//
	// Construction
	//

	public SwingScrollPanePeer(SwingToolkit toolkit, ScrollPane scrollPane) {
		super(toolkit, scrollPane, new SwingScrollPane(scrollPane));

		SwingToolkit.add(scrollPane, jComponent);
		SwingToolkit.copyAwtProperties(scrollPane, jComponent);
	}

	/**
	 * @see java.awt.peer.ScrollPanePeer#childResized(int, int)
	 */
	public void childResized(int width, int height) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.peer.ScrollPanePeer#getHScrollbarHeight()
	 */
	public int getHScrollbarHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.awt.peer.ScrollPanePeer#getVScrollbarWidth()
	 */
	public int getVScrollbarWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.awt.peer.ScrollPanePeer#setScrollPosition(int, int)
	 */
	public void setScrollPosition(int h, int v) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.peer.ScrollPanePeer#setUnitIncrement(java.awt.Adjustable,
	 *      int)
	 */
	public void setUnitIncrement(Adjustable item, int inc) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.peer.ScrollPanePeer#setValue(java.awt.Adjustable, int)
	 */
	public void setValue(Adjustable item, int value) {
		// TODO Auto-generated method stub

	}

	private static class SwingScrollPane extends JScrollPane implements
			ISwingPeer {
		private final ScrollPane awtComponent;

		public SwingScrollPane(ScrollPane awtComponent) {
			this.awtComponent = awtComponent;
		}

		/**
		 * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
		 */
		public Component getAWTComponent() {
			return awtComponent;
		}
	}
}
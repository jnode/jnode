/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
